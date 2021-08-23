
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<prePlease>:请|麻烦;
<preAllPls>:[<prePlease>]<prePassive>;
<preAll>:<preActive>|<preAllPls>;
<how>:怎么使用|怎么用语音操作|怎么说|怎么用;
<helpTarget>:(?<help>.+?);
############# 语法定义 #############

//怎么使用XXX
[<preAll>]<how><helpTarget>;
//XXX怎么使用
[<preAll>]<helpTarget><how>;
