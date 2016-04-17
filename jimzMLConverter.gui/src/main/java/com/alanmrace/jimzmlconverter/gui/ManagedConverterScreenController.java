/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

/**
 *
 * @author amr1
 */
public abstract class ManagedConverterScreenController extends ManagedScreenController {
    
    @Override
    protected ConverterScreenManager getScreenManager() {
        return (ConverterScreenManager) manager;
    }
}
