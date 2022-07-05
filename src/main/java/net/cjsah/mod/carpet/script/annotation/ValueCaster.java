package net.cjsah.mod.carpet.script.annotation;

import net.cjsah.mod.carpet.script.value.AbstractListValue;
import net.cjsah.mod.carpet.script.value.BlockValue;
import net.cjsah.mod.carpet.script.value.BooleanValue;
import net.cjsah.mod.carpet.script.value.EntityValue;
import net.cjsah.mod.carpet.script.value.FormattedTextValue;
import net.cjsah.mod.carpet.script.value.FunctionValue;
import net.cjsah.mod.carpet.script.value.ListValue;
import net.cjsah.mod.carpet.script.value.MapValue;
import net.cjsah.mod.carpet.script.value.NBTSerializableValue;
import net.cjsah.mod.carpet.script.value.NumericValue;
import net.cjsah.mod.carpet.script.value.StringValue;
import net.cjsah.mod.carpet.script.value.ThreadValue;
import net.cjsah.mod.carpet.script.value.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Simple {@link ValueConverter} implementation that casts a {@link Value} into one of its subclasses, either for use directly in parameters or
 * converters, or as an already working middle step.</p>
 * 
 * <p>{@link ValueCaster}s are reused whenever asked for one, since they don't have any complexity.</p>
 * 
 * @see #register(Class, String)
 * 
 * @param <R> The {@link Value} subclass this {@link ValueCaster} casts to
 */
public final class ValueCaster<R> implements ValueConverter<R> // R always extends Value, not explicitly because of type checking
{
    private static final Map<Class<? extends Value>, ValueCaster<? extends Value>> byResult = new HashMap<>();
    static
    {
        register(Value.class, "value");
        register(BlockValue.class, "block");
        register(EntityValue.class, "entity");
        register(FormattedTextValue.class, "formatted text");
        register(FunctionValue.class, "function");
        register(ListValue.class, "list");
        register(MapValue.class, "map");
        register(AbstractListValue.class, "list or similar"); // For LazyListValue basically? Not sure what should use this
        register(NBTSerializableValue.class, "nbt object");
        register(NumericValue.class, "number");
        register(BooleanValue.class, "boolean");
        register(StringValue.class, "string");
        register(ThreadValue.class, "thread");
    }

    private final Class<R> outputType;
    private final String typeName;

    private ValueCaster(Class<R> outputType, String typeName)
    {
        this.outputType = outputType;
        this.typeName = typeName;
    }

    @Override
    public String getTypeName()
    {
        return typeName;
    }

    /**
     * <p>Returns the registered {@link ValueCaster} for the specified outputType.</p>
     * 
     * @param <R>        The type of the {@link ValueCaster} you are looking for
     * @param outputType The class of the {@link Value} the returned {@link ValueCaster} casts to
     * @return The {@link ValueCaster} for the specified outputType
     */
    @SuppressWarnings("unchecked") // Casters are stored with their exact class, for sure since the map is private (&& class has same generic as
                                   // caster)
    public static <R> ValueCaster<R> get(Class<R> outputType)
    {
        return (ValueCaster<R>) byResult.get(outputType);
    }

    @Override
    @SuppressWarnings("unchecked") // more than checked, see SimpleTypeConverter#converter for reasoning
    public R convert(Value value)
    {
        if (!outputType.isInstance(value))
            return null;
        return (R)value;
    }

    /**
     * <p>Registers a new {@link Value} to be able to use it in {@link SimpleTypeConverter}</p>
     * 
     * @param <R>        The {@link Value} subclass
     * @param valueClass The class of T
     * @param typeName   A {@link String} representing the name of this type. It will be used in error messages when there is no higher type
     *                   required<!--, with the form //Outdated concept <code>(function name) requires a (typeName) to be passed as (argName, if
     *                   available)</code>-->
     */
    public static <R extends Value> void register(Class<R> valueClass, String typeName)
    {
        ValueCaster<R> caster = new ValueCaster<R>(valueClass, typeName);
        byResult.putIfAbsent(valueClass, caster);
    }
}
