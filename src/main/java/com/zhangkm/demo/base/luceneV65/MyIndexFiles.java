package com.zhangkm.demo.base.luceneV65;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run
 * it with no command-line arguments for usage information.
 */
public class MyIndexFiles {
	private static final String[] contents = { "我喜欢看电影款发123张动机和顾客和房东来开发股份科技对肌肤两个软件撒林可否认",
			"你好，我是来自正can i search the english word 义网的张克猛同学，我喜欢读书，也喜欢看电影，薄*熙*来",
			"我的名字是张三，我can i search the english word 张在做123一克个lucene的测试，我的爱好猛是写程8序阿飞认可和繁荣热火",
			"张克猛你好！最近薄熙来新闻的事件很敏感，请问张克猛，能检索出来吗？芙蓉枫热歌",
			"中华人民共和国，中华，人123民，共和国，这个是按照8最小切can i search the english word 词还是最大切词原则呢？突然感染人很好题外话",
			"你好！里竟然乖乖听话团购会张克猛能检索出来吗热各位高人我给他还热乎？" };

	private MyIndexFiles() {
	}

	/** Index all text files under a directory. */
	public static void main(String[] args) {
		try {
			System.out.println("Indexing...");

			Directory dir = FSDirectory.open(Paths.get("index"));
			Analyzer analyzer = new CJKAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(dir, iwc);
			for (int i = 0; i < contents.length; i++) {
				Document doc = new Document();
				doc.add(new TextField("id",i+"",Field.Store.YES));
				doc.add(new TextField("content", contents[i], Field.Store.YES));
				writer.addDocument(doc);
			}
			writer.close();
			System.out.println("the end");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}

		try {
			FSDirectory diskDirectory = FSDirectory.open(Paths.get("index"));
			IndexReader reader = DirectoryReader.open(diskDirectory);
			IndexSearcher searcher = new IndexSearcher(reader);

			// searcher.setDefaultFieldSortScoring(true, false);
			// QueryParser queryParser = new MultiFieldQueryParser(new String[]
			// { "title","content" },new CJKAnalyzer());
			QueryParser queryParser = new QueryParser("content", new CJKAnalyzer());

			Query multiFieldQuery = queryParser.parse("薄熙来");
			// TermQuery multiFieldQuery = new TermQuery(new
			// Term("content","张克猛"));

			TopDocs tds = searcher.search(multiFieldQuery, 10);
			for (ScoreDoc sd : tds.scoreDocs) {
				Document doc = searcher.doc(sd.doc);
				System.out.println("id:" + doc.get("id"));
				System.out.println("content:" + contents[Integer.parseInt(doc.get("id"))]);
			}
			// searcher.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
