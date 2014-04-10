package com.michelle.share.util;

public class FileManager {

	public static String getSaveFilePath() {
		if (CommonUtil.hasSDCard()) {
			return CommonUtil.getRootFilePath() + "MichelleShare/";
		} else {
			return CommonUtil.getRootFilePath() + "MichelleShare/";
		}
	}

}
