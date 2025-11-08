package com.tcmyxc.zuul;


import java.io.File;

/**
 * done
 */
public interface DynamicCodeCompiler {

    Class compile(String code, String name) throws Exception;

    Class compile(File file) throws Exception;
}
