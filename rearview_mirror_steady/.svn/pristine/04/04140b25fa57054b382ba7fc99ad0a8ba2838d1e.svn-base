
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<preTell>:告诉|你告诉;
<prePlease>:请|麻烦;
<me>:我;

<askMscNow>:现在正在|现在在|现在|当前|正在|这;
<askMscPlay>:播放|播|放|收听|听;
<askMscOf>:的;
<askMscIs>:是|叫;
<askMscWhat>:什么|啥|嘛|谁的;
<askMscMsc>:歌|歌曲|音乐|曲|曲子|节目;


############# 语法定义 #############
//请告诉我当前播放的歌是什么歌
[[<prePlease>]<preTell>][<me>]<askMscNow><askMscMsc>[<askMscIs>]<askMscWhat>[<askMscMsc>]
//请告诉我这是播的什么歌
[[<prePlease>]<preTell>][<me>]<askMscNow>[<askMscIs>[<askMscPlay>[<askMscOf>]]]<askMscWhat><askMscMsc>
//请告诉我这歌是什么歌
[[<prePlease>]<preTell>][<me>]<askMscNow><askMscPlay>[<askMscOf>][askMscIs][<askMscMsc>][<askMscIs>]<askMscWhat>[<askMscMsc>]
//现在播的节目是什么
[[<prePlease>]<preTell>][<me>]<askMscNow><askMscPlay>[<askMscOf>][<askMscMsc>][<askMscIs>]<askMscWhat>
