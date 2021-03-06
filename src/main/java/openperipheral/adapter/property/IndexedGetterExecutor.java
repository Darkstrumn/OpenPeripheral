package openperipheral.adapter.property;

import com.google.common.base.Preconditions;
import java.lang.reflect.Field;
import openperipheral.api.converter.IConverter;
import org.apache.commons.lang3.ArrayUtils;

public class IndexedGetterExecutor implements IPropertyExecutor {

	private final Field field;

	private final IIndexedFieldManipulator manipulator;

	private final IndexedTypeInfo typeInfo;

	private final IIndexedPropertyAccessHandler accessHandler;

	public IndexedGetterExecutor(Field field, IIndexedFieldManipulator manipulator, IndexedTypeInfo typeInfo, IIndexedPropertyAccessHandler accessHandler) {
		this.field = field;
		this.manipulator = manipulator;
		this.typeInfo = typeInfo;
		this.accessHandler = accessHandler;
	}

	@Override
	public Object[] call(IConverter converter, Object owner, Object... args) {
		Preconditions.checkArgument(args.length == 1, "Getter should have exactly one argument (index)");
		final Object index = converter.toJava(args[0], typeInfo.keyType);
		Preconditions.checkArgument(index != null, "Invalid index");

		final Object target = PropertyUtils.getContents(owner, field);
		accessHandler.onGet(owner, target, field, index);
		final Object result = manipulator.getField(owner, target, field, index);
		final Object converted = converter.fromJava(result);
		return ArrayUtils.toArray(converted);
	}

}
