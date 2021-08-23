
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preAll>:<preActive>|<preAllPls>;
<preAllPls>:[<prePlease>]<prePassive>;

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<preTell>:告诉我|你告诉我;
<prePlease>:请|麻烦;

<kwFlow>:流量;
<remain>:剩余|还有|只有;
<flowUnit>:兆;
<when>:时;
<flowRemind>:提醒我|告诉我;
<flowTar>:(?<flow>.+?);

############# 语法定义 #############
//[流量]剩余XXX兆[时]提醒我
[<preAll>][<kwFlow>]<remain><flowTar><flowUnit>[<when>]<flowRemind>
