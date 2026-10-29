package org.taskdeck.app;

/** Decimal megabytes for display; Android PSS is supplied in KiB. */
public final class MemoryUnits {
    private MemoryUnits(){}
    public static long bytes(long bytes){ return Math.round(bytes/1_000_000d); }
    public static long pss(long kib){ return Math.round(kib*1024d/1_000_000d); }
}
