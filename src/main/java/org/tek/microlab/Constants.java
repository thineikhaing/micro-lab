package org.tek.microlab;

public final class Constants {
	private Constants() {
		throw new IllegalStateException(UTILITY_CLASS);
	}

	public static final String UTILITY_CLASS = "Utility Class";

	public static class BATCH_JOB_ID {
		public static final String DMS = "DMS-BATCH";
		public static final String S3PROCESS = "S3PROCESS-BATCH";
		public static final String SIGNALFILE = "SIGNALFILE-BATCH";
	}

	public static class MARKET_DATA_REPORT_TYPE {
		public static final String BATCH = "B";
	}
}


