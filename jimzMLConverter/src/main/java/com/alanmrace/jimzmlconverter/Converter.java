/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;

/**
 *
 * @author amr1
 */
public interface Converter  {
    public void convert() throws ImzMLConversionException;
}
