DROP TABLE IF EXISTS entry_bbs;
CREATE TABLE entry_bbs(
	entry_id INT PRIMARY KEY, 
	entry_group VARCHAR(255),
	entry_website VARCHAR(255),
	entry_encoding VARCHAR(255),
	entry_jsoup_urls VARCHAR(255),
	page_jsoup_title VARCHAR(255),
	page_jsoup_content VARCHAR(255)
);
DROP TABLE IF EXISTS entry_bbs_channel;
CREATE TABLE entry_bbs_channel(
	channel_id INT PRIMARY KEY, 
	entry_id INT, 
	channel_name VARCHAR(255),
	channel_url VARCHAR(255)
);

INSERT INTO entry_bbs VALUES(1, 'bbs','新浪论坛','gb2312','a[href]','title','body');
INSERT INTO entry_bbs_channel VALUES(1, 1,'新浪杂谈','http://club.history.sina.com.cn/forum-51-1.html');
