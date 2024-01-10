package com.xxx.data.stream;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.Random;

/**
 * @author a1234
 * @description
 * @create 2024-01-10 18:09
 */
public class Demo {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8081);
        //显示指定本地运行并开启web页面
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);

        //2. 创建数据流
        DataStreamSource<String> socket = env.socketTextStream("localhost", 7777);
        //3. Transform
        SingleOutputStreamOperator<String> flatMapStream = socket.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                String[] split = s.split("\\s+");
                for (String s1 : split) {
                    collector.collect(s1);
                }
            }
        });
        SingleOutputStreamOperator<Tuple2<String, Integer>> mapStream = flatMapStream.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String s) throws Exception {
                return Tuple2.of(s,1);
            }
        });

        KeyedStream<Tuple2<String, Integer>, String> keyByStream = mapStream.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return stringIntegerTuple2.f0;
            }
        });

        SingleOutputStreamOperator<Tuple2<String, Integer>> sumStream = keyByStream.sum(1);

        //4. 打印输出
        sumStream.print();

        //5. 提交运行
        env.execute();

    }

}
