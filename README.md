# ClassTimetables
仿超级课程表/Android课程表/简洁

---
# 界面
<p>
<img src="https://raw.githubusercontent.com/Potato-DiGua/ClassTimetables/master/ReadmeResourse/gui.png" height="30%" width="30%">
<p/>
<p>
<img src="https://raw.githubusercontent.com/Potato-DiGua/ClassTimetables/master/ReadmeResourse/guitool.png" height="30%" width="30%">
<p/>
<p>
<img src="https://raw.githubusercontent.com/Potato-DiGua/ClassTimetables/master/ReadmeResourse/gui_course.png" height="30%" width="30%">
<p/>

---
# 注意事项
支持Excel导入（**因为使用jxl包所以只支持xls文件**）<br>
不支持冲突课程，如果冲突，会显示最后的课程<br>
Excel格式示例
<p>
<img src="https://raw.githubusercontent.com/Potato-DiGua/ClassTimetables/master/ReadmeResourse/Excel_example.png" height="50%" width="50%">
<p/>

**单元格格式:**
<br>
第一行为课程名<br>
第二行为教授<br>
第三行为上课的周数（单周，双周，周用[]括起来,周数也可以用英文,隔开，中间没有空格）<br>
示例**1-16[单周]**
或**7,8,9[周]**
<br>
第四行为上课地点<br>
<br>
课程信息用回车隔开，同一单元格有两个课程空一行隔开<br>

<a href="https://raw.githubusercontent.com/Potato-DiGua/ClassTimetables/master/ReadmeResourse/Example.xls">下载示例Excel<a/>

**第一行必须为星期，第一列为节数，不能有多余的表头，也不能有多余的内容**。<br>
节次也可以改为一个单元格一节课。<br>
一天只支持12节课。<br>

**所以Excel算上表头只支持(列数x行数)8x7或8x13**

---
##### 开发者的废话(可跳过)
因为超级课程表有很多多余的东西，而且想看课程表的话，打开速度很慢，不能秒开，所以就想模仿写一个精简版的，于是就有了本app


