package com.snmp.poller.utils.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snmp.poller.Env;
import com.snmp.poller.enums.MsgLevel;
import com.snmp.poller.model.UserCurrentInfo;

public class FileUtils {
	private static Logger log = LoggerFactory.getLogger(FileUtils.class);

	public static String output(List<UserCurrentInfo> entities) throws Exception {
		String dateStamp = Env.FORMAT_YYYYMMDDHHMISS_NO_SYMBOL.format(new Date());
		final String filePath = Env.OUTPUT_FILE_PATH.concat(File.separator).concat(Env.FORMAT_YYYYMMDD_NO_SLASH.format(new Date()));
		final String fileName = StringUtils.replace(Env.OUTPUT_FILE_NAME, "*", dateStamp);
		Path pathFileDir = Paths.get(filePath);
		Path pathFile = Paths.get(filePath.concat(File.separator).concat(fileName));
		BufferedWriter bw = null;

		try {
			if (entities != null && !entities.isEmpty()) {
				if (StringUtils.isBlank(Env.OUTPUT_FILE_PATH)) {
					throw new Exception("系統環境參數未設定 >> Env.OUTPUT_FILE_PATH: ["+Env.OUTPUT_FILE_PATH+"]");
				}

				if (!Files.isDirectory(pathFileDir, LinkOption.NOFOLLOW_LINKS)) {
					Files.createDirectory(pathFileDir);
				}

				bw = Files.newBufferedWriter(pathFile, StandardCharsets.UTF_8);

				int rowNo = 1;
				for (UserCurrentInfo entity : entities) {
					String[] array = entity.toArray();
					array[0] = String.valueOf(rowNo);

					int idx = 0;
					for (String str : array) {
						bw.write(str != null ? str : "");

						if (idx < array.length-1) {
							bw.write(Env.OUTPUT_FILE_SEPARATOR_SYMBOL);
						}

						idx++;
					}

					bw.write(System.lineSeparator());
					rowNo++;
				}

				bw.flush();
			}

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, FileUtils.class, e.toString());

		} finally {
			if (bw != null) {
				bw.close();
			}
		}

		return filePath.substring(filePath.lastIndexOf("\\")+1);
	}
}
