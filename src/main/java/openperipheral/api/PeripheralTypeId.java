package openperipheral.api;

import java.lang.annotation.*;

/**
 * Used for creating custom names for peripherals. Useable only on TileEntities.
 *
 * @author boq
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PeripheralTypeId {
	public String value();
}
