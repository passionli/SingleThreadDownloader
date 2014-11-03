SingleThreadDownloader
======================

TODO:<br>
1.数据库处理的接口与实现分离，方便以后换不同的实现方案<br>
2.下载任务的优先级调整，参考操作系统是如何实现优先级调度的<br>
3.把下载工作移到Service中处理<br>
4.参考Google提供的DownloadManager，把任务用ContentProvider封装起来。阅读DownloadManager相关源码。<br>
5.封装与一个Android library，供他人使用<br>
6.参考google play等应用的下载功能，尽量做到功能上类似<br>
7.用HashMap保存数据库任务的缓存<br>
8.开发者可以设置下载文件的类型（apk,jpg,html,mp3等）及本地路径<br>
9.阅读《java并发编程实战》，及时优化相关代码模块<br>
10.参考github上别人写的下载库，学习其中的优点，并设法在本项目中运用起来<br>
11.下载过程中用户清理了SD卡缓存如何解决？检查文件是否存在？重新下载一个完整的文件？<br>
