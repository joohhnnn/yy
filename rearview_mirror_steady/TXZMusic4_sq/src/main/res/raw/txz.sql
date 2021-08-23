-- ----------------------------
-- Table structure for album
-- ----------------------------
DROP TABLE IF EXISTS 'album';
CREATE TABLE 'album' (
    '_index' integer primary key,
    'sid' int(11) NOT NULL,
    'id' bigint(20) NOT NULL,
    'name' varchar(255) DEFAULT NULL,
    'report' varchar(255) DEFAULT NULL,
    'logo' varchar(255) DEFAULT NULL,
    'desc' varchar(255) DEFAULT NULL,
    'arrArtistName' text,
    'likedNum' int(11) DEFAULT NULL,
    'listenNum' bigint(20) DEFAULT NULL,
    'arrCategoryIds' varchar(255) DEFAULT NULL,
    'categoryId' int(11),
    'lastUpdate' bigint(20),
    'properties' int(11),
    'lastListen' bigint(20)
);

-- ----------------------------
-- Table structure for audio
-- ----------------------------
DROP TABLE IF EXISTS 'audio';
CREATE TABLE 'audio' (
    'sid' int(11) NOT NULL,
    'id' int(11) NOT NULL,
    'sourceFrom' varchar(255),
    'albumId' varchar(255) DEFAULT NULL,
    'desc' varchar(255) DEFAULT NULL,
    'name' varchar(255) DEFAULT NULL,
    'logo' varchar(255) DEFAULT NULL,
    'report' varchar(255) DEFAULT NULL,
    'createTime' bigint(20) DEFAULT NULL,
    'duration' bigint(20) DEFAULT NULL,
    'fileSize' bigint(20) DEFAULT NULL,
    'arrArtistName' varchar(255) DEFAULT '',
    'artistNames' varchar(255) DEFAULT NULL,
    'likedNum' int(11) DEFAULT NULL,
    'listenNum' bigint(11) DEFAULT NULL,
    'url' varchar(255) DEFAULT NULL,
    'lastPlayTime' varchar(255),
    'strCategoryId' varchar(255) DEFAULT NULL,
    'currentPlayTime' varchar(255) DEFAULT NULL,
    'strCategoryDesc' varchar(255) DEFAULT NULL,
    'bNoCache' boolean DEFAULT NULL,
    'downloadType' varchar(255) DEFAULT NULL,
    'strDownloadUrl' varchar(255) DEFAULT NULL,
    'strProcessingUrl' varchar(255) DEFAULT NULL,
    'iExpTime' int DEFAULT NULL,
    'bShowSource' boolean,
    'albumName' varchar(255),
    'pinyin'  varchar(255),
    'urlType' int(11),
    'clientListenNum' int(11),
    'flag' int(11),
    'orderNum' int(11),
    PRIMARY KEY ('id','sid')
);
-- ----------------------------
-- Table structure for historyaudio
-- ----------------------------
DROP TABLE IF EXISTS 'historyaudio';
CREATE TABLE 'historyaudio' (
    'indexId' INTEGER PRIMARY KEY autoincrement,
    'sid' int(11) NOT NULL,
    'id' int(11) NOT NULL,
    'albumId' varchar(255) DEFAULT NULL,
    'desc' varchar(255) DEFAULT NULL,
    'name' varchar(255) DEFAULT NULL,
    'logo' varchar(255) DEFAULT NULL,
    'createTime' bigint(20) DEFAULT NULL,
    'duration' bigint(20) DEFAULT NULL,
    'fileSize' bigint(20) DEFAULT NULL,
    'arrArtistName' varchar(255) DEFAULT '',
    'report' varchar(255) DEFAULT NULL,
    'artistNames' varchar(255) DEFAULT NULL,
    'likedNum' int(11) DEFAULT NULL,
    'listenNum' bigint(11) DEFAULT NULL,
    'url' varchar(255) DEFAULT NULL,
    'lastPlayTime' varchar(255),
    'strCategoryId' varchar(255) DEFAULT NULL,
    'currentPlayTime' varchar(255) DEFAULT NULL,
    'strCategoryDesc' varchar(255) DEFAULT NULL,
    'bNoCache' boolean DEFAULT NULL,
    'downloadType' varchar(255) DEFAULT NULL,
    'strDownloadUrl' varchar(255) DEFAULT NULL,
    'strProcessingUrl' varchar(255) DEFAULT NULL,
    'iExpTime' int DEFAULT NULL,
    'bShowSource' boolean,
    'urlType' int(11),
    'sourceFrom' varchar(255),
    'flag' int(11),
    'orderNum' int(11),
    'albumName' varchar(255)
);
--上次播放的歌单
DROP TABLE IF EXISTS 'LASTAUDIOS';

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS 'category';
CREATE TABLE 'category' (
    'categoryId' varchar(255) NOT NULL,
    'desc' varchar(255) DEFAULT NULL,
    'logo' varchar(255) DEFAULT NULL,
    'arrChild' text,
    'drawableId' int(11) DEFAULT NULL,
    PRIMARY KEY ('categoryId')
) ;


-- ----------------------------
-- Table structure for responsealbumaudio
-- ----------------------------
DROP TABLE IF EXISTS 'responsealbumaudio';

-- ----------------------------
-- Table structure for responsesearchalbum
-- ----------------------------
DROP TABLE IF EXISTS 'responsesearchalbum';

-- ----------------------------
-- Table structure for respcheck
-- ----------------------------
DROP TABLE IF EXISTS 'respcheck';

-- ----------------------------
-- Table structure for cache
-- ----------------------------
DROP TABLE IF EXISTS 'cache';
-- -----------
-- Table localAudio 本地音乐
-- ----------
drop table if EXISTS 'localaudio';
create table 'localaudio'(
    'sid' int(11) DEFAULT 0,
    'id' int(11) NOT NULL,
    'albumId' varchar(255) DEFAULT NULL,
    'desc' varchar(255) DEFAULT NULL,
    'name' varchar(255) DEFAULT NULL,
    'logo' varchar(255) DEFAULT NULL,
    'createTime' bigint(20) ,
    'duration' bigint(20) ,
    'fileSize' bigint(20) ,
    'arrArtistName' varchar(255) DEFAULT NULL,
    'artistNames' varchar(255) DEFAULT NULL,
    'likedNum' int(11) DEFAULT NULL,
    'listenNum' bigint(11) DEFAULT NULL,
    'url' varchar(255) DEFAULT NULL,
    'lastPlayTime' varchar(255),
    'strCategoryId' varchar(255) DEFAULT NULL,
    'currentPlayTime' varchar(255) DEFAULT NULL,
    'strCategoryDesc' varchar(255) DEFAULT NULL,
    'bNoCache' boolean DEFAULT NULL,
    'downloadType' varchar(255) DEFAULT NULL,
    'strDownloadUrl' varchar(255) DEFAULT NULL,
    'report' varchar(255) DEFAULT NULL,
    'strProcessingUrl' varchar(255) DEFAULT NULL,
    'pinyin'  varchar(255),
    'urlType' int(11),
    'sourceFrom' varchar(255),
    'bShowSource' boolean,
    PRIMARY KEY ('strDownloadUrl')
);


DROP TABLE IF EXISTS 'breakpoint';
CREATE TABLE 'breakpoint'(
    'indexId' INTEGER PRIMARY KEY autoincrement,
    'sid' int(11) NOT NULL,
    'id' int(11) NOT NULL,
    'albumId' varchar(255) DEFAULT NULL,
    'name' varchar(255) DEFAULT NULL,
    'createTime' bigint(20) DEFAULT NULL,
    'lastPlayTime' varchar(255)
);