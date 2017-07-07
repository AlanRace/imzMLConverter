/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlparser.mzml.BinaryDataArray.CompressionType;
import com.beust.jcommander.IStringConverter;

/**
 *
 * @author alan.race
 */
public class CompressionConverter implements IStringConverter<CompressionType> {

    @Override
    public CompressionType convert(String string) {
        return CompressionType.valueOf(string);
    }
}
