
############# 别名定义 #############

#混乱字符
<~allChar>:(?:   (?:_[a-z]+\d_)  |  (?:[a-zA-Z]{1,3})  );
<allChaos>:(?: (?:<~allChar>)* );

<preMe>:这|朕|正|我;
<preNeed>:要|想|想要;
<wkkHelp>:帮你|替你|给你|为你;
<wkkBa>:把;
<wkkWakeupKw>:唤醒词|唤醒时|换信纸|换信时;
<wkkCmd>:取|起|换|更换|命|改|修改;
<wkkAmount>:个|一|一个;
<wkkName>:名|名字|新名字|新名;
<wkkFeng>:册封你|封你|册封以为|封以为;
<wkkAs>:叫做|为|叫|成;
<wkkPreName>:你好|您好;
<wkkNameValue>:(?<name>.+?);
<wkkKingValue>:(?<king>.+?);
<wkkSetValue>:(?<set>.+?);
#我要帮你取个名字为
<wkkGrammarName>:[[<preMe>[<preNeed>]]<wkkHelp>]<wkkCmd>[<wkkAmount>]<wkkName><wkkAs>[<wkkPreName>]<wkkNameValue>;
#朕封你为xxx
<wkkGrammarKing>:[<preMe>[<preNeed>]]<wkkFeng><wkkAs><wkkKingValue>;
#我要帮你把唤醒词改为xxx
<wkkGrammarSet1>:[[[<preMe>[<preNeed>]]<wkkHelp>]<wkkBa>]<wkkWakeupKw><wkkCmd><wkkAs>;
#我要帮你修改唤醒词为xxx
<wkkGrammarSet2>:[[<preMe>[<preNeed>]]<wkkHelp>]<wkkCmd><wkkWakeupKw><wkkAs>;

<wkkGrammarSet>:(?:<wkkGrammarSet1>|<wkkGrammarSet2>)<wkkSetValue>;

<wkkStart>:<wkkGrammarName>|<wkkGrammarKing>|<wkkGrammarSet>;



############# 语法定义 #############
<wkkStart>;
