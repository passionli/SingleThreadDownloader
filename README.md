SingleThreadDownloader
======================

TODO:
1.数据库处理的接口与实现分离，方便以后换不同的实现方案
2.下载任务的优先级调整，参考操作系统是如何实现优先级调度的
3.把下载工作移到Service中处理
4.参考Google提供的DownloadManager，把任务用ContentProvider封装起来。阅读DownloadManager相关源码。
5.封装与一个Android library，供他人使用
6.参考google play等应用的下载功能，尽量做到功能上类似
7.用HashMap保存数据库任务的缓存
8.开发者可以设置下载文件的类型（apk,jpg,html,mp3等）及本地路径
9.阅读《java并发编程实战》，及时优化相关代码模块
10.参考github上别人写的下载库，学习其中的优点，并设法在本项目中运用起来
11.
