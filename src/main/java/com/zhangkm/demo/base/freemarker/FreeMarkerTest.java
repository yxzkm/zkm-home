package com.zhangkm.demo.base.freemarker;

import freemarker.template.*;
import java.util.*;
import java.io.*;

public class FreeMarkerTest {

	// freemaker官方例子
	public static void main(String[] args) throws Exception {

		/* You should do this ONLY ONCE in the whole application life-cycle: */

		/* Create and adjust the configuration singleton */
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		cfg.setDirectoryForTemplateLoading(new File("./resources/template/demo/base"));
		cfg.setDefaultEncoding("UTF-8");               
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);

		/*
		 * You usually do these for MULTIPLE TIMES in the application
		 * life-cycle:
		 */

		/* Create a data-model */
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("user", "Big Joe");
		Product latest = new Product();
		latest.setUrl("products/greenmouse.html");
		latest.setName("green mouse");
		root.put("latestProduct", latest);

		/* Get the template (uses cache internally) */
		OutputStream out = null;
		Writer writer = null;
		try {
			Template temp = cfg.getTemplate("freeMakerTestTemplate.ftlh");

			/* Merge data-model with template */
			out = new FileOutputStream("./resources/template/dmeo/base/freeMakerTestTemplate.html");// 打印到文件
			writer = new OutputStreamWriter(out);
			temp.process(root, writer);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Note: Depending on what `out` is, you may need to call `out.close()`.
		// This is usually the case for file output, but not for servlet output.
	}

}
