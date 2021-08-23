package com.txznet.txz.module.netdata;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.txznet.sdk.bean.WeatherData.WeatherDay;
import com.txznet.txz.module.netdata.WeatherData1.DataEntity.ResultEntity;
import com.txznet.txz.module.netdata.WeatherData1.DataEntity.ResultEntity.WeatherDaysEntity;
import com.txznet.txz.module.netdata.WeatherData1.SemanticEntity.IntentEntity;


/**
 * 
 * 天气信息<br />
 * 例：<br />
 * {
    "rc": 0,
    "text": "深圳天气怎样",
    "service": "cn.yunzhisheng.weather",
    "code": "FORECAST",
    "semantic": {
        "intent": {
            "province": "广东省",
            "city": "深圳",
            "cityCode": "101280601",
            "focusDate": "2015-10-22",
            "topic": "WEATHER"
        }
    },
    "data": {
        "header": "深圳今天天气情况是：晴，24至31，微风级(无持续风向)。",
        "result": {
            "weatherDays": [{
                "year": 2015,
                "month": 10,
                "day": 22,
                "dayOfWeek": 5,
                "weather": "晴",
                "highestTemperature": 31,
                "lowestTemperature": 24,
                "currentTemperature": 30,
                "pm2_5": 31,
                "quality": "良",
                "wind": "微风级(无持续风向)",
                "imageTitleOfDay": "",
                "imageTitleOfNight": "",
                "comfortIndex": "较不舒适",
                "comfortIndexDesc": "白天天气晴好，明媚的阳光在给您带来好心情的同时，也会使您感到有些热，不很舒适。",
                "carWashIndex": "较适宜",
                "carWashIndexDesc": "较适宜洗车，未来两天无雨，风力较小，擦洗一新的汽车至少能保持两天。",
                "dressIndex": "炎热",
                "dressIndexDesc": "天气炎热，建议着短衫、短裙、短裤、薄型T恤衫等清凉夏季服装。",
                "sunBlockIndex": "很强",
                "sunBlockIndexDesc": "紫外线辐射极强，建议涂擦SPF20以上、PA++的防晒护肤品，尽量避免暴露于日光下。",
                "sportIndex": "",
                "sportIndexDesc": "天气较好，户外运动请注意防晒。推荐您进行室内运动。",
                "dryingIndex": "极适宜",
                "dryingIndexDesc": "天气不错，极适宜晾晒。抓紧时机把久未见阳光的衣物搬出来晒晒太阳吧！",
                "morningExerciseIndex": "适宜",
                "morningExerciseIndexDesc": "天气不错，空气清新，是您晨练的大好时机，建议不同年龄段的人们积极参加户外健身活动。",
                "coldIndex": "少发",
                "coldIndexDesc": "各项气象条件适宜，发生感冒机率较低。但请避免长期处于空调房间中，以防感冒。",
                "datingIndex": "较不适宜",
                "datingIndexDesc": "天气较热，建议尽量不要去室外约会，如果外出，请您挑选荫凉的地点。",
                "umbrellaIndex": "不带伞",
                "umbrellaIndexDesc": "天气较好，您在出门的时候无须带雨伞。",
                "travelIndex": "适宜",
                "travelIndexDesc": "天气较好，但稍感觉有些热，不过还是个好天气哦。适宜旅游，可不要错过机会呦！",
                "suggest": "极少数敏感人群应减少户外活动"
            },
            {
                "year": 2015,
                "month": 10,
                "day": 23,
                "dayOfWeek": 6,
                "weather": "多云",
                "highestTemperature": 30,
                "lowestTemperature": 23,
                "quality": "",
                "wind": "微风级(无持续风向)",
                "imageTitleOfDay": "",
                "imageTitleOfNight": "",
                "comfortIndex": "",
                "comfortIndexDesc": "",
                "carWashIndex": "较适宜",
                "carWashIndexDesc": "较适宜洗车，未来一天无雨，风力较小，擦洗一新的汽车至少能保持一天。",
                "dressIndex": "",
                "dressIndexDesc": "",
                "sunBlockIndex": "",
                "sunBlockIndexDesc": "",
                "sportIndex": "",
                "sportIndexDesc": "",
                "dryingIndex": "",
                "dryingIndexDesc": "",
                "morningExerciseIndex": "",
                "morningExerciseIndexDesc": "",
                "coldIndex": "",
                "coldIndexDesc": "",
                "datingIndex": "",
                "datingIndexDesc": "",
                "umbrellaIndex": "",
                "umbrellaIndexDesc": "",
                "travelIndex": "",
                "travelIndexDesc": "",
                "suggest": ""
            },
            {
                "year": 2015,
                "month": 10,
                "day": 24,
                "dayOfWeek": 7,
                "weather": "多云",
                "highestTemperature": 29,
                "lowestTemperature": 23,
                "quality": "",
                "wind": "微风级(无持续风向)",
                "imageTitleOfDay": "",
                "imageTitleOfNight": "",
                "comfortIndex": "",
                "comfortIndexDesc": "",
                "carWashIndex": "不宜",
                "carWashIndexDesc": "明天有雨，如果洗车，擦洗一新的汽车保持不到一天。",
                "dressIndex": "",
                "dressIndexDesc": "",
                "sunBlockIndex": "",
                "sunBlockIndexDesc": "",
                "sportIndex": "",
                "sportIndexDesc": "",
                "dryingIndex": "",
                "dryingIndexDesc": "",
                "morningExerciseIndex": "",
                "morningExerciseIndexDesc": "",
                "coldIndex": "",
                "coldIndexDesc": "",
                "datingIndex": "",
                "datingIndexDesc": "",
                "umbrellaIndex": "",
                "umbrellaIndexDesc": "",
                "travelIndex": "",
                "travelIndexDesc": "",
                "suggest": ""
            },
            {
                "year": 2015,
                "month": 10,
                "day": 25,
                "dayOfWeek": 1,
                "weather": "小雨",
                "highestTemperature": 29,
                "lowestTemperature": 22,
                "quality": "",
                "wind": "微风级(无持续风向)",
                "imageTitleOfDay": "",
                "imageTitleOfNight": "",
                "comfortIndex": "",
                "comfortIndexDesc": "",
                "carWashIndex": "不宜",
                "carWashIndexDesc": "不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。",
                "dressIndex": "",
                "dressIndexDesc": "",
                "sunBlockIndex": "",
                "sunBlockIndexDesc": "",
                "sportIndex": "",
                "sportIndexDesc": "",
                "dryingIndex": "",
                "dryingIndexDesc": "",
                "morningExerciseIndex": "",
                "morningExerciseIndexDesc": "",
                "coldIndex": "",
                "coldIndexDesc": "",
                "datingIndex": "",
                "datingIndexDesc": "",
                "umbrellaIndex": "",
                "umbrellaIndexDesc": "",
                "travelIndex": "",
                "travelIndexDesc": "",
                "suggest": ""
            },
            {
                "year": 2015,
                "month": 10,
                "day": 26,
                "dayOfWeek": 2,
                "weather": "小雨",
                "highestTemperature": 28,
                "lowestTemperature": 21,
                "quality": "",
                "wind": "微风级(无持续风向)",
                "imageTitleOfDay": "",
                "imageTitleOfNight": "",
                "comfortIndex": "",
                "comfortIndexDesc": "",
                "carWashIndex": "不宜",
                "carWashIndexDesc": "不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。",
                "dressIndex": "",
                "dressIndexDesc": "",
                "sunBlockIndex": "",
                "sunBlockIndexDesc": "",
                "sportIndex": "",
                "sportIndexDesc": "",
                "dryingIndex": "",
                "dryingIndexDesc": "",
                "morningExerciseIndex": "",
                "morningExerciseIndexDesc": "",
                "coldIndex": "",
                "coldIndexDesc": "",
                "datingIndex": "",
                "datingIndexDesc": "",
                "umbrellaIndex": "",
                "umbrellaIndexDesc": "",
                "travelIndex": "",
                "travelIndexDesc": "",
                "suggest": ""
            }],
            "cityName": "深圳",
            "cityCode": "101280601",
            "updateTime": "2015-10-22 17:00:53",
            "focusDateIndex": 0,
            "errorCode": 0
        }
    },
    "general": {
        "type": "T",
        "text": "深圳今天天气情况是：晴，24至31，微风级(无持续风向)。"
    },
    "history": "cn.yunzhisheng.weather",
    "responseId": "8e45d18362e54202beeed9210351d849"
}
 *
 */
public class WeatherData1 {
    /**
     * rc : 0
     * text : 深圳天气怎样
     * service : cn.yunzhisheng.weather
     * code : FORECAST
     * semantic : {"intent":{"province":"广东省","city":"深圳","cityCode":"101280601","focusDate":"2015-10-22","topic":"WEATHER"}}
     * data : {"header":"深圳今天天气情况是：晴，24至31，微风级(无持续风向)。","result":{"weatherDays":[{"year":2015,"month":10,"day":22,"dayOfWeek":5,"weather":"晴","highestTemperature":31,"lowestTemperature":24,"currentTemperature":30,"pm2_5":31,"quality":"良","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"较不舒适","comfortIndexDesc":"白天天气晴好，明媚的阳光在给您带来好心情的同时，也会使您感到有些热，不很舒适。","carWashIndex":"较适宜","carWashIndexDesc":"较适宜洗车，未来两天无雨，风力较小，擦洗一新的汽车至少能保持两天。","dressIndex":"炎热","dressIndexDesc":"天气炎热，建议着短衫、短裙、短裤、薄型T恤衫等清凉夏季服装。","sunBlockIndex":"很强","sunBlockIndexDesc":"紫外线辐射极强，建议涂擦SPF20以上、PA++的防晒护肤品，尽量避免暴露于日光下。","sportIndex":"","sportIndexDesc":"天气较好，户外运动请注意防晒。推荐您进行室内运动。","dryingIndex":"极适宜","dryingIndexDesc":"天气不错，极适宜晾晒。抓紧时机把久未见阳光的衣物搬出来晒晒太阳吧！","morningExerciseIndex":"适宜","morningExerciseIndexDesc":"天气不错，空气清新，是您晨练的大好时机，建议不同年龄段的人们积极参加户外健身活动。","coldIndex":"少发","coldIndexDesc":"各项气象条件适宜，发生感冒机率较低。但请避免长期处于空调房间中，以防感冒。","datingIndex":"较不适宜","datingIndexDesc":"天气较热，建议尽量不要去室外约会，如果外出，请您挑选荫凉的地点。","umbrellaIndex":"不带伞","umbrellaIndexDesc":"天气较好，您在出门的时候无须带雨伞。","travelIndex":"适宜","travelIndexDesc":"天气较好，但稍感觉有些热，不过还是个好天气哦。适宜旅游，可不要错过机会呦！","suggest":"极少数敏感人群应减少户外活动"},{"year":2015,"month":10,"day":23,"dayOfWeek":6,"weather":"多云","highestTemperature":30,"lowestTemperature":23,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"较适宜","carWashIndexDesc":"较适宜洗车，未来一天无雨，风力较小，擦洗一新的汽车至少能保持一天。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":24,"dayOfWeek":7,"weather":"多云","highestTemperature":29,"lowestTemperature":23,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"明天有雨，如果洗车，擦洗一新的汽车保持不到一天。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":25,"dayOfWeek":1,"weather":"小雨","highestTemperature":29,"lowestTemperature":22,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":26,"dayOfWeek":2,"weather":"小雨","highestTemperature":28,"lowestTemperature":21,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""}],"cityName":"深圳","cityCode":"101280601","updateTime":"2015-10-22 17:00:53","focusDateIndex":0,"errorCode":0}}
     * general : {"type":"T","text":"深圳今天天气情况是：晴，24至31，微风级(无持续风向)。"}
     * history : cn.yunzhisheng.weather
     * responseId : 8e45d18362e54202beeed9210351d849
     */

    private int rc;
    private String text;
    private String service;
    private String code;
    /**
     * intent : {"province":"广东省","city":"深圳","cityCode":"101280601","focusDate":"2015-10-22","topic":"WEATHER"}
     */

    private SemanticEntity semantic;
    /**
     * header : 深圳今天天气情况是：晴，24至31，微风级(无持续风向)。
     * result : {"weatherDays":[{"year":2015,"month":10,"day":22,"dayOfWeek":5,"weather":"晴","highestTemperature":31,"lowestTemperature":24,"currentTemperature":30,"pm2_5":31,"quality":"良","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"较不舒适","comfortIndexDesc":"白天天气晴好，明媚的阳光在给您带来好心情的同时，也会使您感到有些热，不很舒适。","carWashIndex":"较适宜","carWashIndexDesc":"较适宜洗车，未来两天无雨，风力较小，擦洗一新的汽车至少能保持两天。","dressIndex":"炎热","dressIndexDesc":"天气炎热，建议着短衫、短裙、短裤、薄型T恤衫等清凉夏季服装。","sunBlockIndex":"很强","sunBlockIndexDesc":"紫外线辐射极强，建议涂擦SPF20以上、PA++的防晒护肤品，尽量避免暴露于日光下。","sportIndex":"","sportIndexDesc":"天气较好，户外运动请注意防晒。推荐您进行室内运动。","dryingIndex":"极适宜","dryingIndexDesc":"天气不错，极适宜晾晒。抓紧时机把久未见阳光的衣物搬出来晒晒太阳吧！","morningExerciseIndex":"适宜","morningExerciseIndexDesc":"天气不错，空气清新，是您晨练的大好时机，建议不同年龄段的人们积极参加户外健身活动。","coldIndex":"少发","coldIndexDesc":"各项气象条件适宜，发生感冒机率较低。但请避免长期处于空调房间中，以防感冒。","datingIndex":"较不适宜","datingIndexDesc":"天气较热，建议尽量不要去室外约会，如果外出，请您挑选荫凉的地点。","umbrellaIndex":"不带伞","umbrellaIndexDesc":"天气较好，您在出门的时候无须带雨伞。","travelIndex":"适宜","travelIndexDesc":"天气较好，但稍感觉有些热，不过还是个好天气哦。适宜旅游，可不要错过机会呦！","suggest":"极少数敏感人群应减少户外活动"},{"year":2015,"month":10,"day":23,"dayOfWeek":6,"weather":"多云","highestTemperature":30,"lowestTemperature":23,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"较适宜","carWashIndexDesc":"较适宜洗车，未来一天无雨，风力较小，擦洗一新的汽车至少能保持一天。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":24,"dayOfWeek":7,"weather":"多云","highestTemperature":29,"lowestTemperature":23,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"明天有雨，如果洗车，擦洗一新的汽车保持不到一天。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":25,"dayOfWeek":1,"weather":"小雨","highestTemperature":29,"lowestTemperature":22,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":26,"dayOfWeek":2,"weather":"小雨","highestTemperature":28,"lowestTemperature":21,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""}],"cityName":"深圳","cityCode":"101280601","updateTime":"2015-10-22 17:00:53","focusDateIndex":0,"errorCode":0}
     */

    private DataEntity data;
    /**
     * type : T
     * text : 深圳今天天气情况是：晴，24至31，微风级(无持续风向)。
     */

    private GeneralEntity general;
    private String history;
    private String responseId;

    public void setRc(int rc) {
        this.rc = rc;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSemantic(SemanticEntity semantic) {
        this.semantic = semantic;
    }

    public void setData(DataEntity data) {
        this.data = data;
    }

    public void setGeneral(GeneralEntity general) {
        this.general = general;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public int getRc() {
        return rc;
    }

    public String getText() {
        return text;
    }

    public String getService() {
        return service;
    }

    public String getCode() {
        return code;
    }

    public SemanticEntity getSemantic() {
        return semantic;
    }

    public DataEntity getData() {
        return data;
    }

    public GeneralEntity getGeneral() {
        return general;
    }

    public String getHistory() {
        return history;
    }

    public String getResponseId() {
        return responseId;
    }

    public static class SemanticEntity {
        /**
         * province : 广东省
         * city : 深圳
         * cityCode : 101280601
         * focusDate : 2015-10-22
         * topic : WEATHER
         */

        private IntentEntity intent;

        public void setIntent(IntentEntity intent) {
            this.intent = intent;
        }

        public IntentEntity getIntent() {
            return intent;
        }

        public static class IntentEntity {
            private String province;
            private String city;
            private String cityCode;
            private String focusDate;
            private String topic;

            public void setProvince(String province) {
                this.province = province;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public void setCityCode(String cityCode) {
                this.cityCode = cityCode;
            }

            public void setFocusDate(String focusDate) {
                this.focusDate = focusDate;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }

            public String getProvince() {
                return province;
            }

            public String getCity() {
                return city;
            }

            public String getCityCode() {
                return cityCode;
            }

            public String getFocusDate() {
                return focusDate;
            }

            public String getTopic() {
                return topic;
            }
        }
    }

    public static class DataEntity {
        private String header;
        /**
         * weatherDays : [{"year":2015,"month":10,"day":22,"dayOfWeek":5,"weather":"晴","highestTemperature":31,"lowestTemperature":24,"currentTemperature":30,"pm2_5":31,"quality":"良","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"较不舒适","comfortIndexDesc":"白天天气晴好，明媚的阳光在给您带来好心情的同时，也会使您感到有些热，不很舒适。","carWashIndex":"较适宜","carWashIndexDesc":"较适宜洗车，未来两天无雨，风力较小，擦洗一新的汽车至少能保持两天。","dressIndex":"炎热","dressIndexDesc":"天气炎热，建议着短衫、短裙、短裤、薄型T恤衫等清凉夏季服装。","sunBlockIndex":"很强","sunBlockIndexDesc":"紫外线辐射极强，建议涂擦SPF20以上、PA++的防晒护肤品，尽量避免暴露于日光下。","sportIndex":"","sportIndexDesc":"天气较好，户外运动请注意防晒。推荐您进行室内运动。","dryingIndex":"极适宜","dryingIndexDesc":"天气不错，极适宜晾晒。抓紧时机把久未见阳光的衣物搬出来晒晒太阳吧！","morningExerciseIndex":"适宜","morningExerciseIndexDesc":"天气不错，空气清新，是您晨练的大好时机，建议不同年龄段的人们积极参加户外健身活动。","coldIndex":"少发","coldIndexDesc":"各项气象条件适宜，发生感冒机率较低。但请避免长期处于空调房间中，以防感冒。","datingIndex":"较不适宜","datingIndexDesc":"天气较热，建议尽量不要去室外约会，如果外出，请您挑选荫凉的地点。","umbrellaIndex":"不带伞","umbrellaIndexDesc":"天气较好，您在出门的时候无须带雨伞。","travelIndex":"适宜","travelIndexDesc":"天气较好，但稍感觉有些热，不过还是个好天气哦。适宜旅游，可不要错过机会呦！","suggest":"极少数敏感人群应减少户外活动"},{"year":2015,"month":10,"day":23,"dayOfWeek":6,"weather":"多云","highestTemperature":30,"lowestTemperature":23,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"较适宜","carWashIndexDesc":"较适宜洗车，未来一天无雨，风力较小，擦洗一新的汽车至少能保持一天。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":24,"dayOfWeek":7,"weather":"多云","highestTemperature":29,"lowestTemperature":23,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"明天有雨，如果洗车，擦洗一新的汽车保持不到一天。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":25,"dayOfWeek":1,"weather":"小雨","highestTemperature":29,"lowestTemperature":22,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""},{"year":2015,"month":10,"day":26,"dayOfWeek":2,"weather":"小雨","highestTemperature":28,"lowestTemperature":21,"quality":"","wind":"微风级(无持续风向)","imageTitleOfDay":"","imageTitleOfNight":"","comfortIndex":"","comfortIndexDesc":"","carWashIndex":"不宜","carWashIndexDesc":"不宜洗车，今天有雨，如果洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","dressIndex":"","dressIndexDesc":"","sunBlockIndex":"","sunBlockIndexDesc":"","sportIndex":"","sportIndexDesc":"","dryingIndex":"","dryingIndexDesc":"","morningExerciseIndex":"","morningExerciseIndexDesc":"","coldIndex":"","coldIndexDesc":"","datingIndex":"","datingIndexDesc":"","umbrellaIndex":"","umbrellaIndexDesc":"","travelIndex":"","travelIndexDesc":"","suggest":""}]
         * cityName : 深圳
         * cityCode : 101280601
         * updateTime : 2015-10-22 17:00:53
         * focusDateIndex : 0
         * errorCode : 0
         */

        private ResultEntity result;

        public void setHeader(String header) {
            this.header = header;
        }

        public void setResult(ResultEntity result) {
            this.result = result;
        }

        public String getHeader() {
            return header;
        }

        public ResultEntity getResult() {
            return result;
        }

        public static class ResultEntity {
            private String cityName;
            private String cityCode;
            private String updateTime;
            private int focusDateIndex;
            private int errorCode;
            /**
             * year : 2015
             * month : 10
             * day : 22
             * dayOfWeek : 5
             * weather : 晴
             * highestTemperature : 31
             * lowestTemperature : 24
             * currentTemperature : 30
             * pm2_5 : 31
             * quality : 良
             * wind : 微风级(无持续风向)
             * imageTitleOfDay :
             * imageTitleOfNight :
             * comfortIndex : 较不舒适
             * comfortIndexDesc : 白天天气晴好，明媚的阳光在给您带来好心情的同时，也会使您感到有些热，不很舒适。
             * carWashIndex : 较适宜
             * carWashIndexDesc : 较适宜洗车，未来两天无雨，风力较小，擦洗一新的汽车至少能保持两天。
             * dressIndex : 炎热
             * dressIndexDesc : 天气炎热，建议着短衫、短裙、短裤、薄型T恤衫等清凉夏季服装。
             * sunBlockIndex : 很强
             * sunBlockIndexDesc : 紫外线辐射极强，建议涂擦SPF20以上、PA++的防晒护肤品，尽量避免暴露于日光下。
             * sportIndex :
             * sportIndexDesc : 天气较好，户外运动请注意防晒。推荐您进行室内运动。
             * dryingIndex : 极适宜
             * dryingIndexDesc : 天气不错，极适宜晾晒。抓紧时机把久未见阳光的衣物搬出来晒晒太阳吧！
             * morningExerciseIndex : 适宜
             * morningExerciseIndexDesc : 天气不错，空气清新，是您晨练的大好时机，建议不同年龄段的人们积极参加户外健身活动。
             * coldIndex : 少发
             * coldIndexDesc : 各项气象条件适宜，发生感冒机率较低。但请避免长期处于空调房间中，以防感冒。
             * datingIndex : 较不适宜
             * datingIndexDesc : 天气较热，建议尽量不要去室外约会，如果外出，请您挑选荫凉的地点。
             * umbrellaIndex : 不带伞
             * umbrellaIndexDesc : 天气较好，您在出门的时候无须带雨伞。
             * travelIndex : 适宜
             * travelIndexDesc : 天气较好，但稍感觉有些热，不过还是个好天气哦。适宜旅游，可不要错过机会呦！
             * suggest : 极少数敏感人群应减少户外活动
             */

            private List<WeatherDaysEntity> weatherDays;

            public void setCityName(String cityName) {
                this.cityName = cityName;
            }

            public void setCityCode(String cityCode) {
                this.cityCode = cityCode;
            }

            public void setUpdateTime(String updateTime) {
                this.updateTime = updateTime;
            }

            public void setFocusDateIndex(int focusDateIndex) {
                this.focusDateIndex = focusDateIndex;
            }

            public void setErrorCode(int errorCode) {
                this.errorCode = errorCode;
            }

            public void setWeatherDays(List<WeatherDaysEntity> weatherDays) {
                this.weatherDays = weatherDays;
            }

            public String getCityName() {
                return cityName;
            }

            public String getCityCode() {
                return cityCode;
            }

            public String getUpdateTime() {
                return updateTime;
            }

            public int getFocusDateIndex() {
                return focusDateIndex;
            }

            public int getErrorCode() {
                return errorCode;
            }

            public List<WeatherDaysEntity> getWeatherDays() {
                return weatherDays;
            }

            public static class WeatherDaysEntity {
                private int year;
                private int month;
                private int day;
                private int dayOfWeek;
                private String weather;
                private int highestTemperature;
                private int lowestTemperature;
                private int currentTemperature;
                private int pm2_5;
                private String quality;
                private String wind;
                private String imageTitleOfDay;
                private String imageTitleOfNight;
                private String comfortIndex;
                private String comfortIndexDesc;
                private String carWashIndex;
                private String carWashIndexDesc;
                private String dressIndex;
                private String dressIndexDesc;
                private String sunBlockIndex;
                private String sunBlockIndexDesc;
                private String sportIndex;
                private String sportIndexDesc;
                private String dryingIndex;
                private String dryingIndexDesc;
                private String morningExerciseIndex;
                private String morningExerciseIndexDesc;
                private String coldIndex;
                private String coldIndexDesc;
                private String datingIndex;
                private String datingIndexDesc;
                private String umbrellaIndex;
                private String umbrellaIndexDesc;
                private String travelIndex;
                private String travelIndexDesc;
                private String suggest;

                public void setYear(int year) {
                    this.year = year;
                }

                public void setMonth(int month) {
                    this.month = month;
                }

                public void setDay(int day) {
                    this.day = day;
                }

                public void setDayOfWeek(int dayOfWeek) {
                    this.dayOfWeek = dayOfWeek;
                }

                public void setWeather(String weather) {
                    this.weather = weather;
                }

                public void setHighestTemperature(int highestTemperature) {
                    this.highestTemperature = highestTemperature;
                }

                public void setLowestTemperature(int lowestTemperature) {
                    this.lowestTemperature = lowestTemperature;
                }

                public void setCurrentTemperature(int currentTemperature) {
                    this.currentTemperature = currentTemperature;
                }

                public void setPm2_5(int pm2_5) {
                    this.pm2_5 = pm2_5;
                }

                public void setQuality(String quality) {
                    this.quality = quality;
                }

                public void setWind(String wind) {
                    this.wind = wind;
                }

                public void setImageTitleOfDay(String imageTitleOfDay) {
                    this.imageTitleOfDay = imageTitleOfDay;
                }

                public void setImageTitleOfNight(String imageTitleOfNight) {
                    this.imageTitleOfNight = imageTitleOfNight;
                }

                public void setComfortIndex(String comfortIndex) {
                    this.comfortIndex = comfortIndex;
                }

                public void setComfortIndexDesc(String comfortIndexDesc) {
                    this.comfortIndexDesc = comfortIndexDesc;
                }

                public void setCarWashIndex(String carWashIndex) {
                    this.carWashIndex = carWashIndex;
                }

                public void setCarWashIndexDesc(String carWashIndexDesc) {
                    this.carWashIndexDesc = carWashIndexDesc;
                }

                public void setDressIndex(String dressIndex) {
                    this.dressIndex = dressIndex;
                }

                public void setDressIndexDesc(String dressIndexDesc) {
                    this.dressIndexDesc = dressIndexDesc;
                }

                public void setSunBlockIndex(String sunBlockIndex) {
                    this.sunBlockIndex = sunBlockIndex;
                }

                public void setSunBlockIndexDesc(String sunBlockIndexDesc) {
                    this.sunBlockIndexDesc = sunBlockIndexDesc;
                }

                public void setSportIndex(String sportIndex) {
                    this.sportIndex = sportIndex;
                }

                public void setSportIndexDesc(String sportIndexDesc) {
                    this.sportIndexDesc = sportIndexDesc;
                }

                public void setDryingIndex(String dryingIndex) {
                    this.dryingIndex = dryingIndex;
                }

                public void setDryingIndexDesc(String dryingIndexDesc) {
                    this.dryingIndexDesc = dryingIndexDesc;
                }

                public void setMorningExerciseIndex(String morningExerciseIndex) {
                    this.morningExerciseIndex = morningExerciseIndex;
                }

                public void setMorningExerciseIndexDesc(String morningExerciseIndexDesc) {
                    this.morningExerciseIndexDesc = morningExerciseIndexDesc;
                }

                public void setColdIndex(String coldIndex) {
                    this.coldIndex = coldIndex;
                }

                public void setColdIndexDesc(String coldIndexDesc) {
                    this.coldIndexDesc = coldIndexDesc;
                }

                public void setDatingIndex(String datingIndex) {
                    this.datingIndex = datingIndex;
                }

                public void setDatingIndexDesc(String datingIndexDesc) {
                    this.datingIndexDesc = datingIndexDesc;
                }

                public void setUmbrellaIndex(String umbrellaIndex) {
                    this.umbrellaIndex = umbrellaIndex;
                }

                public void setUmbrellaIndexDesc(String umbrellaIndexDesc) {
                    this.umbrellaIndexDesc = umbrellaIndexDesc;
                }

                public void setTravelIndex(String travelIndex) {
                    this.travelIndex = travelIndex;
                }

                public void setTravelIndexDesc(String travelIndexDesc) {
                    this.travelIndexDesc = travelIndexDesc;
                }

                public void setSuggest(String suggest) {
                    this.suggest = suggest;
                }

                public int getYear() {
                    return year;
                }

                public int getMonth() {
                    return month;
                }

                public int getDay() {
                    return day;
                }

                public int getDayOfWeek() {
                    return dayOfWeek;
                }

                public String getWeather() {
                    return weather;
                }

                public int getHighestTemperature() {
                    return highestTemperature;
                }

                public int getLowestTemperature() {
                    return lowestTemperature;
                }

                public int getCurrentTemperature() {
                    return currentTemperature;
                }

                public int getPm2_5() {
                    return pm2_5;
                }

                public String getQuality() {
                    return quality;
                }

                public String getWind() {
                    return wind;
                }

                public String getImageTitleOfDay() {
                    return imageTitleOfDay;
                }

                public String getImageTitleOfNight() {
                    return imageTitleOfNight;
                }

                public String getComfortIndex() {
                    return comfortIndex;
                }

                public String getComfortIndexDesc() {
                    return comfortIndexDesc;
                }

                public String getCarWashIndex() {
                    return carWashIndex;
                }

                public String getCarWashIndexDesc() {
                    return carWashIndexDesc;
                }

                public String getDressIndex() {
                    return dressIndex;
                }

                public String getDressIndexDesc() {
                    return dressIndexDesc;
                }

                public String getSunBlockIndex() {
                    return sunBlockIndex;
                }

                public String getSunBlockIndexDesc() {
                    return sunBlockIndexDesc;
                }

                public String getSportIndex() {
                    return sportIndex;
                }

                public String getSportIndexDesc() {
                    return sportIndexDesc;
                }

                public String getDryingIndex() {
                    return dryingIndex;
                }

                public String getDryingIndexDesc() {
                    return dryingIndexDesc;
                }

                public String getMorningExerciseIndex() {
                    return morningExerciseIndex;
                }

                public String getMorningExerciseIndexDesc() {
                    return morningExerciseIndexDesc;
                }

                public String getColdIndex() {
                    return coldIndex;
                }

                public String getColdIndexDesc() {
                    return coldIndexDesc;
                }

                public String getDatingIndex() {
                    return datingIndex;
                }

                public String getDatingIndexDesc() {
                    return datingIndexDesc;
                }

                public String getUmbrellaIndex() {
                    return umbrellaIndex;
                }

                public String getUmbrellaIndexDesc() {
                    return umbrellaIndexDesc;
                }

                public String getTravelIndex() {
                    return travelIndex;
                }

                public String getTravelIndexDesc() {
                    return travelIndexDesc;
                }

                public String getSuggest() {
                    return suggest;
                }
            }
        }
    }

    public static class GeneralEntity {
        private String type;
        private String text;

        public void setType(String type) {
            this.type = type;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
    
    public static String getDefaultWeatherInfo(){
    	WeatherData1 data = new WeatherData1();
    	data.setRc(0);
    	data.setCode("0");
    	data.setHistory("cn.yunzhisheng.weather");
    	data.setService("cn.yunzhisheng.weather");
    	data.setResponseId("10000");
    	data.setText("今天天气怎么样");
    	
    	GeneralEntity oGeneralEntity = new GeneralEntity();
    	oGeneralEntity.setText("天气");
    	oGeneralEntity.setType("T");
    	data.setGeneral(oGeneralEntity);
    	
    	SemanticEntity oSemanticEntity = new SemanticEntity();
    	IntentEntity oIntentEntity = new IntentEntity();
    	oIntentEntity.setCity("未知");
    	oIntentEntity.setCityCode("0");
    	oIntentEntity.setFocusDate("0");
    	oIntentEntity.setProvince("未知");
    	oIntentEntity.setTopic("");
    	oSemanticEntity.setIntent(oIntentEntity);
    	data.setSemantic(oSemanticEntity);
    	
    	DataEntity oDataEntity = new DataEntity();
    	oDataEntity.setHeader("");
    	
    	ResultEntity oResultEntity = new ResultEntity();
    	oResultEntity.setCityCode(null);
    	oResultEntity.setCityName(null);
    	List<WeatherDaysEntity> weatherDays = new ArrayList<WeatherDaysEntity>();
    	for (int i = 0; i < 1; i++){
    		WeatherDaysEntity day = new WeatherDaysEntity();
    		day.setWeather("未知");
    		day.setYear(2017);
    		day.setMonth(1);
    		day.setDay(20);
    		weatherDays.add(day);
    	}
    	oResultEntity.setWeatherDays(weatherDays);
    	oDataEntity.setResult(oResultEntity);
    	data.setData(oDataEntity);
    	String strData = "";
		try {
			JSONObject jsonData = (JSONObject) JSONObject.toJSON(data);
			strData = jsonData.toString();
		} catch (Exception e) {
		}
    	return strData;
    }
}

