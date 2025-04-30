import core.ConstExp
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class MyIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        pluginContext.log("Start")

        moduleFragment.transformChildrenVoid(
            LogsTransformer(pluginContext = pluginContext),
        )

        pluginContext.log("End")
    }

    private fun IrPluginContext.log(message: Any) {
        messageCollector.report(CompilerMessageSeverity.INFO, ">>>" + message)
    }
}

private class LogsTransformer(private val pluginContext: IrPluginContext) : IrElementTransformerVoid() {
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val printlnFunctionSymbol = pluginContext.referenceFunctions(
        CallableId(FqName("kotlin.io"), Name.identifier("println")),
    ).first { it.owner.valueParameters.singleOrNull()?.type == pluginContext.irBuiltIns.anyNType }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        val hasAnnotation = declaration.annotations.hasAnnotation(FqName(ConstExp::class.qualifiedName!!))
        if (hasAnnotation) {
            val result = transformFunction(declaration)
            if (result != null) return result
        }
        return super.visitSimpleFunction(declaration)
    }

    private fun transformFunction(function: IrSimpleFunction): IrStatement? {
        val originalBody = function.body as? IrBlockBody
        val originalStatements = originalBody?.statements?.toList() ?: emptyList()

        val irBuilder = pluginContext.irBuiltIns.createIrBuilder(function.symbol)

        function.body = irBuilder.irBlockBody {
            +irCall(printlnFunctionSymbol).apply {
                putValueArgument(index = 0, valueArgument = irString("Logging here"))
            }

            originalStatements.forEach { statement ->
                +statement
            }
        }

        return function
    }
}