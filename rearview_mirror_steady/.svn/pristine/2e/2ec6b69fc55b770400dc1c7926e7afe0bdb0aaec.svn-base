
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<prePlease>:请|麻烦;
<preAllPls>:[<prePlease>]<prePassive>;
<preAll>:<preActive>|<preAllPls>;

<nwsCmd>:听|播放|播|放|来|点播|点|收听;
<nwsStart>:[<preAll>]<nwsCmd>[<nwsAmount>]<nwsTarget>;

//一段关于xxx相关的新闻
<nwsTarget>:[[<nwsInc>]<nwsKw>[<nwsRef>][<nwsOf>]]<nwsNews>[<allChaos>];

<nwsCmd>:听|播放|播|放|来|点播|点|收听|打开;
<nwsAmount>:个|一个|段|一段|则|一则|一;
<nwsInc>:包含|关于|有关;
<nwsRef>:有关|相关|类|类型|分类;
<nwsKw>:(?<kw>.*?);
<nwsNews>:新闻;
<nwsOf>:的;


############# 语法定义 #############
//播放[一个][[关于]XXX[相关][的]]新闻
[<preAll>]<nwsCmd>[<nwsAmount>]<nwsTarget>
