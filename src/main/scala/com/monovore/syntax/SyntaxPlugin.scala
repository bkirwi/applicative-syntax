package com.monovore.syntax

import scala.tools.nsc.Global
import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.transform.Transform

class SyntaxPlugin(val global: Global) extends Plugin { plugin =>

  val name = "applicative-syntax"

  val description = "Syntax for applicative-style for comprehensions"

  val components = SyntaxComponent :: Nil

  object SyntaxComponent extends PluginComponent with Transform with TreeDSL {

    override val global: Global = plugin.global

    import global._

    override val phaseName: String = "applicative-syntax"

    override val runsAfter: List[String] = "parser" :: Nil

    override protected def newTransformer(unit: CompilationUnit): Transformer = new Transformer {
      override def transform(tree: Tree): Tree = {

        def isApplicative(annotation: Tree): Boolean = annotation match {
          case Apply(Select(New(Ident(TypeName("applicative"))), termNames.CONSTRUCTOR), List()) => true
          case _ => false
        }

        def rewrite(tree: Tree, values: List[Tree], params: List[ValDef]): Tree = tree match {
          case Apply(Select(lhs, TermName("flatMap")), List(Function(List(valDef), body))) =>
            rewrite(body, lhs :: values, valDef :: params)
          case Apply(Select(lhs, TermName("map")), List(Function(List(valDef), body))) =>

            val allValues = (lhs :: values).reverse
            val allParams = (valDef :: params).reverse

            val tupleType = Select(Ident("scala"), TermName("Tuple" + allValues.length))
            val tuple = Apply(tupleType, allValues)
            Apply(Select(tuple, TermName("mapN")), List(Function(allParams, body)))
          case m @ Match(_, _) =>
            reporter.error(tree.pos, "Applicative for-comprehension desugaring doesn't support assignment!")
            tree
          case other =>
            reporter.error(tree.pos, "Got unexpected tree in applicative desugaring: " + other)
            tree
        }

        val x = tree match {
          case Annotated(annotation, value) if isApplicative(annotation) =>
            rewrite(value, Nil, Nil)
          case value: ValDef if value.mods.annotations.exists(isApplicative) =>
            val remainingAnnotations = value.mods.annotations.filterNot(isApplicative)
            val rewritten = rewrite(value.rhs, Nil, Nil)
            value.copy(rhs = rewritten, mods = value.mods.copy(annotations = remainingAnnotations))
          case _ => tree
        }

        super.transform(x)
      }
    }
  }
}