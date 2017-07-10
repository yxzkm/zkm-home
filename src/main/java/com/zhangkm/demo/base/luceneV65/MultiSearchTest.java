package com.zhangkm.demo.base.luceneV65;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class MultiSearchTest {
	public static FSDirectory DISK_WORK_DIRECTORY;
	public static RAMDirectory RAM_WORK_DIRECTORY;
	public static Analyzer ANALYZER = null;

	private String[] ids = {"0","1","2","3","4","5"};
	private String[] types = {"MBLOG","NEWS","MBLOG","MBLOG","NEWS","BBS",};
	private String[] titles = {
			"南丰路两份开发就两个人",
			"科技人太少:两兄弟股份",
			"诶乳房附近哦啊风范德萨减肥",
			"会花费了地方更浪费规范何虹健",
			"科技虽薄熙来然金融机构解放东路",
			"法拉克和张克猛归属感立刻梵蒂冈"};
	private String[] contents = {
			"我喜欢看电影款发张动机和顾客和房东来开发股份科技对肌肤两个软件撒林可否认",
			"你好，我是来自正义网的张克猛同学，我喜欢读书，也喜欢看电影，薄*熙*来",
			"我的名字是张三，我张在做一克个lucene的测试，我的爱好猛是写程序阿飞认可和繁荣热火",
			"张克猛你好！最近薄熙来新闻的事件很敏感，请问张克猛，能检索出来吗？芙蓉枫热歌",
			"中华人民共和国，中华，人民，共和国，这个是按照最小切词还是最大切词原则呢？突然感染人很好题外话",
			"你好！里竟然乖乖听话团购会张克猛能检索出来吗热各位高人我给他还热乎？"
	};
	private Date[] dates = null;
	private int[] attachs = {2,3,1,4,5,5};
	private Map<String,Float> scoresMap = new HashMap<String,Float>();

	public MultiSearchTest(){
		setDates();
		scoresMap.put("BBS", 1.0f);
		scoresMap.put("NEWS", 1.0f);
		scoresMap.put("MBLOG", 1.0f);
		ANALYZER = new CJKAnalyzer(); //IK最小分词原则
		try {
			DISK_WORK_DIRECTORY = FSDirectory.open(Paths.get("d:/lucene/multi/work"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		RAM_WORK_DIRECTORY = new RAMDirectory();
	}

	private void setDates() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			dates = new Date[ids.length];
			dates[0] = sdf.parse("2010-02-19");
			dates[1] = sdf.parse("2012-01-11");
			dates[2] = sdf.parse("2011-09-19");
			dates[3] = sdf.parse("2010-12-22");
			dates[4] = sdf.parse("2012-01-01");
			dates[5] = sdf.parse("2011-05-19");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void makeMultiIndex(String flag){
		//定义临时内存索引Directory
		RAMDirectory tempRAMDirectory = null; 
		//定义长期内存索引Writer
		IndexWriter totalRamWriter = null;
		
		try {
			//初始化长期内存索引Writer
			totalRamWriter = new IndexWriter(RAM_WORK_DIRECTORY, new IndexWriterConfig(ANALYZER));

			//向临时内存索引写入数据
			tempRAMDirectory = new RAMDirectory();
			writeIndex(tempRAMDirectory,flag);
			
			//将临时内存中的索引数据,追加到长期内存中
			totalRamWriter.addIndexes(tempRAMDirectory);
			
			//将临时内存中的索引数据,持久化到磁盘中,以时间戳作为磁盘目录名称
			IndexWriter diskWriter = null;
			try {
				FSDirectory diskDirectory = FSDirectory.open(Paths.get("d:/lucene/multi/temp/"+System.currentTimeMillis()));
				diskWriter = new IndexWriter(diskDirectory, new IndexWriterConfig(ANALYZER));
				diskWriter.addIndexes(tempRAMDirectory);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(diskWriter!=null) diskWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				if(totalRamWriter!=null) totalRamWriter.close();	
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally{
				try {
					if(RAM_WORK_DIRECTORY!=null&& IndexWriter.isLocked(RAM_WORK_DIRECTORY)){
						//IndexWriter.unlock(RAM_WORK_DIRECTORY);
						System.out.println("注意：强行将Lucene磁盘文件解锁");
					}
				} catch (Exception e1) {
					System.out.println("error:磁盘文件解锁失败");
					e1.printStackTrace();
				}
			}
		}		
	}
	
	private void writeIndex(Directory directory,String from){
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(directory, new IndexWriterConfig(ANALYZER));
			Document doc = null;
			for(int i=0;i<ids.length;i++) {
				doc = new Document();
				doc.add(new StringField("id",ids[i],Field.Store.YES));
				doc.add(new StringField("type",types[i],Field.Store.YES));
				doc.add(new StringField("from",from,Field.Store.YES));
				doc.add(new StringField("title",titles[i],Field.Store.YES));
				doc.add(new StringField("content",contents[i],Field.Store.NO));
				
//				//存储数字
//				doc.add(new NumericField("attach",Field.Store.YES,true).setIntValue(attachs[i]));
//				//存储日期
//				doc.add(new NumericField("date",Field.Store.YES,true).setLongValue(dates[i].getTime()));
//
//				if(scoresMap.containsKey(types[i])) {
//					doc.setBoost(scoresMap.get(types[i]));
//				} else {
//					doc.setBoost(0.5f);
//				}

				writer.addDocument(doc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer!=null)writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public IndexSearcher getMultiSearcher() {
		try {
			IndexReader[] readers = new IndexReader[2];
			
			readers[0] = DirectoryReader.open(RAM_WORK_DIRECTORY);
			readers[1] = DirectoryReader.open(DISK_WORK_DIRECTORY);

			MultiReader multiReader = new MultiReader(readers);
			IndexSearcher searcher = new IndexSearcher(multiReader);
			return searcher;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public IndexSearcher getRamSearcher() {
		try {
			IndexReader reader = DirectoryReader.open(RAM_WORK_DIRECTORY);
			IndexSearcher searcher = new IndexSearcher(reader);
			return searcher;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public IndexSearcher getDiskSearcher() {
		try {
			IndexReader reader = DirectoryReader.open(DISK_WORK_DIRECTORY);
			IndexSearcher searcher = new IndexSearcher(reader);
			return searcher;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void mutiFieldsQuery(IndexSearcher searcher) {
		try {
			//searcher.setDefaultFieldSortScoring(true, false);
			QueryParser queryParser = new MultiFieldQueryParser(new String[] { "title","content" },ANALYZER);

			Query multiFieldQuery = queryParser.parse("张克猛 薄熙来");
			//TermQuery multiFieldQuery = new TermQuery(new Term("content","张克猛"));
			
			
			TopDocs tds = searcher.search(multiFieldQuery, 10);
			for(ScoreDoc sd:tds.scoreDocs) {
				Document doc = searcher.doc(sd.doc);
				System.out.println("from:"+doc.get("from"));
				System.out.println("id:"+doc.get("id"));
//				System.out.println("type:"+doc.get("type"));
//				System.out.println("title:"+titles[Integer.parseInt(doc.get("id"))]);
//				System.out.println("content:"+contents[Integer.parseInt(doc.get("id"))]);
//				System.out.println("sd.score:"+sd.score);
			}
			//searcher.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mergeTemp2Work(){
		IndexWriter writer = null;
		try {
			//打开磁盘工作目录，准备进行写操作
			writer = new IndexWriter(DISK_WORK_DIRECTORY, new IndexWriterConfig(ANALYZER));
			
			//遍历临时磁盘目录,将其中所有子目录中的索引数据,全部合并到磁盘工作目录
			File tempDir = new File("d:/lucene/multi/temp");
			if (!tempDir.exists() || !tempDir.isDirectory()) return ;
			File[] subFiles = tempDir.listFiles();
			for (File file:subFiles) {
				if(file.isDirectory()){
					FSDirectory tempDiskDirectory = FSDirectory.open(Paths.get(file.getAbsolutePath()));
					writer.addIndexes(tempDiskDirectory);
					if(deleteDirectory(file.getAbsolutePath())){
						writer.commit();
					}else{
						writer.rollback();
					}
				}else{
					file.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer!=null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void clearRamDirectory(){
		IndexWriter writer = null;
		try {
			//打开磁盘工作目录，准备进行写操作
			writer = new IndexWriter(RAM_WORK_DIRECTORY, new IndexWriterConfig(ANALYZER));
			writer.deleteAll();
			writer.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer!=null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				try {
					if(RAM_WORK_DIRECTORY!=null&& IndexWriter.isLocked(RAM_WORK_DIRECTORY)){
						//IndexWriter.unlock(RAM_WORK_DIRECTORY);
						System.out.println("注意：强行将Lucene磁盘文件解锁");
					}
				} catch (Exception e1) {
					System.out.println("error:磁盘文件解锁失败");
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static boolean deleteDirectory(String dirName) {
		File dirFile = new File(dirName);
		if (!dirFile.exists() || !dirFile.isDirectory()) return false;
		File[] files = dirFile.listFiles();
		for (File file:files) {
			if (file.isFile()) {
				if(!file.delete()) return false;
			}else {
				if(!deleteDirectory(file.getAbsolutePath())) return false;
			}
		}
		if(!dirFile.delete()) return false;
		return true;
	}	        

	public static void main(String[] args){
		MultiSearchTest msiu = new MultiSearchTest();
		
		msiu.makeMultiIndex("time_1");
		msiu.makeMultiIndex("time_2");
		msiu.makeMultiIndex("time_3");
		msiu.makeMultiIndex("time_4");
		msiu.mergeTemp2Work();
		msiu.clearRamDirectory();


		msiu.mutiFieldsQuery(msiu.getMultiSearcher());

		msiu.makeMultiIndex("time_11");
		msiu.makeMultiIndex("time_12");
		msiu.makeMultiIndex("time_13");
		msiu.makeMultiIndex("time_14");
		System.out.println("after add================================================");
		
		msiu.mutiFieldsQuery(msiu.getMultiSearcher());

		msiu.mergeTemp2Work();
		msiu.clearRamDirectory();
		System.out.println("after merge and clear================================================");
		
		msiu.mutiFieldsQuery(msiu.getMultiSearcher());

		msiu.makeMultiIndex("time_19");
		msiu.makeMultiIndex("time_20");
		msiu.makeMultiIndex("time_21");
		msiu.makeMultiIndex("time_22");
		System.out.println("after add================================================");

		msiu.mutiFieldsQuery(msiu.getMultiSearcher());
	}

}
