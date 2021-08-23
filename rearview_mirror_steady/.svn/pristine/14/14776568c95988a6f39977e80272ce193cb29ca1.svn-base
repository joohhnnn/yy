
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preActive>:我要|我想|我想要;
<prePassive>:帮我|替我|给我|帮忙|你给我|你替我;
<prePlease>:请|麻烦;
<preAllPls>:[<prePlease>]<prePassive>;
<preAll>:<preActive>|<preAllPls>;

<cmdAppOpen>:打开|启动|运行;
<cmdAppClose>:关闭|退出|关掉;
<appOpenCmd>:(?<open><cmdAppOpen>);
<appCloseCmd>:(?<close><cmdAppClose>);
<cmdApp>:(?<app>.+?);

############# 语法定义 #############
[<preAll>]<appOpenCmd><cmdApp>
[<preAll>]<appCloseCmd><cmdApp>
