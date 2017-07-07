/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlparser.data.DataTypeTransform.DataType;
import com.beust.jcommander.IStringConverter;

/**
 *
 * @author alan.race
 */
public class DataTypeConverter implements IStringConverter<DataType> {

    @Override
    public DataType convert(String string) {
        return DataType.valueOf(string);
    }
    
}
