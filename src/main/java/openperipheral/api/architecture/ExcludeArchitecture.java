package openperipheral.api.architecture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import openperipheral.api.Constants;

/**
 * Used to mark method or property that shouldn't be visible in some architectures.
 */
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeArchitecture {
	/**
	 * Excluded architectures. See {@link Constants}.
	 */
	public String[] value();
}
