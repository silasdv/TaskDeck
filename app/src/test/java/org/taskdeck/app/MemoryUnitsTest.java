package org.taskdeck.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class MemoryUnitsTest {
    @Test public void convertsSystemBytesToDecimalMegabytes(){ assertEquals(1000,MemoryUnits.bytes(1_000_000_000L)); assertEquals(1074,MemoryUnits.bytes(1_073_741_824L)); assertEquals(-2,MemoryUnits.bytes(-2_000_000)); }
    @Test public void convertsAndroidPssKibToMegabytes(){ assertEquals(52,MemoryUnits.pss(51200)); assertEquals(1,MemoryUnits.pss(1024)); assertEquals(0,MemoryUnits.pss(0)); }
}
