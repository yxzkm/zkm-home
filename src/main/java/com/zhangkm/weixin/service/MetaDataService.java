package com.zhangkm.weixin.service;

/*
 * Copyright 2002-2017 Drew Noakes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Showcases the most popular ways of using the metadata-extractor library.
 * <p>
 * For more information, see the project wiki: https://github.com/drewnoakes/metadata-extractor/wiki/GettingStarted
 *
 * @author Drew Noakes https://drewnoakes.com
 */
@Service("metaDataService")
public class MetaDataService{
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getPhotoCreateTimeFromMetaData(String fullPathFileName){
        File file = new File(fullPathFileName);

        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (Exception e) {
        	return null;
        }
    	
        if(metadata==null) return null; 
        for (Directory directory : metadata.getDirectories()) {
        	if(directory==null) continue;
            for (Tag tag : directory.getTags()) {
            	if(tag==null) continue;
            	if("Date/Time".equalsIgnoreCase(tag.getTagName())){
            		try {
						Date date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(tag.getDescription());
	            		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
					} catch (ParseException e) {
						return null;
					}
            	}
            }
        }
    	return null;
    }

	public void printPhotoMetaData(String fileName){
        File file = new File(fileName);
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            print(metadata);
        } catch (ImageProcessingException e) {
            // handle exception
        } catch (IOException e) {
            // handle exception
        }

        try {
            Metadata metadata = JpegMetadataReader.readMetadata(file);
            print(metadata);
        } catch (JpegProcessingException e) {
            // handle exception
        } catch (IOException e) {
            // handle exception
        }

        try {
            // We are only interested in handling
            Iterable<JpegSegmentMetadataReader> readers = Arrays.asList(new ExifReader(), new IptcReader());
            Metadata metadata = JpegMetadataReader.readMetadata(file, readers);
            print(metadata);
        } catch (JpegProcessingException e) {
            // handle exception
        } catch (IOException e) {
            // handle exception
        }
    }

    private void print(Metadata metadata){
    	logger.info("-------------------------------------");
        for (Directory directory : metadata.getDirectories()) {

            for (Tag tag : directory.getTags()) {
            	logger.info(tag.toString());
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                	logger.error("ERROR: " + error);
                }
            }
        }
    }
}