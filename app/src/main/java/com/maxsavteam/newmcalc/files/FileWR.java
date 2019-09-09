package com.maxsavteam.newmcalc.files;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileWR { //File Writer/Reader
	public String readFrom(String path) throws FileNotFoundException, IOException {
		String content = "";
		File f = new File(Environment.getExternalStorageDirectory() + "/" + path);
		FileReader fileReader = new FileReader(f);
		while(fileReader.ready()){
			content += (char) fileReader.read();
		}
		fileReader.close();
		return content;
	}
}
