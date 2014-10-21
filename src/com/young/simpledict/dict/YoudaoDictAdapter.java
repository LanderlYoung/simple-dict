package com.young.simpledict.dict;

import android.net.Uri;
import com.young.common.YLog;
import com.young.simpledict.service.model.DictDetail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: taylorcyang
 * Date:   2014-10-21
 * Time:   19:02
 * Life with passion. Code with creativity!
 */
public class YoudaoDictAdapter implements DictAdapter {
    private static final String TAG = "YoudaoDictAdapter";

    @Override
    public String getSearchUri(String mWordToSearch) {
        return "http://dict.youdao.com/jsonapi?client=mobile&" +
                "q=" + Uri.encode(mWordToSearch) +
                "&dicts=%7B%22dicts%22%3A%5B%5B%22ec%22%2C%22ce%22%2C%22cj%22%2C%22jc%22%2C%22kc%22%2C%22ck%22%2C%22fc%22%2C%22cf%22%2C%22media_sents_part%22%2C%22pic_dict%22%2C%22auth_sents_part%22%2C%22rel_word%22%2C%22syno%22%2C%22ec21%22%2C%22ee%22%2C%22special%22%5D%2C%5B%22hh%22%5D%2C%5B%22typos%22%5D%2C%5B%22wordform%22%5D%2C%5B%22ce_new%22%5D%2C%5B%22fanyi%22%5D%5D%2C%22count%22%3A99%7D&keyfrom=mdict.5.3.1.android&" +
                "vendor=youdaoweb&abtest=1&xmlVersion=5.1";
    }

    @Override
    public DictDetail parseDict(String response) {
        DictDetail d = new DictDetail();
        try {
            JSONObject json = new JSONObject(response);
            parseHeader(json.getJSONObject("simple"), d);
            parseEC(json.getJSONObject("ec"), d);
        } catch (JSONException e) {
            YLog.i(TAG, "parse response json failed", e);
        }
        return d;
    }

    private void parseHeader(JSONObject simple, DictDetail d) throws JSONException {
        if (simple != null) {
            d.word = simple.getString("query");
            JSONArray word = simple.getJSONArray("word");
            for (int i = 0; i < word.length(); i++) {
                JSONObject o = word.getJSONObject(i);
                if (o.has("phone")) d.pinyin = o.getString("phone");
                if (o.has("usphone")) d.usphontic = o.getString("usphone");
                if (o.has("ukphone")) d.ukphontic = o.getString("ukphone");
                if (o.has("usspeech")) d.usspeech = o.getString("usspeech") == null ?
                        null : "http://dict.youdao.com/dictvoice?audio=" +
                        o.getString("usspeech");
                if (o.has("ukspeech")) d.ukspeech = o.getString("ukspeech") == null ?
                        null : "http://dict.youdao.com/dictvoice?audio=" +
                        o.getString("ukspeech");
            }
        }
    }

    private void parseEC(JSONObject ec, DictDetail d) throws JSONException {
        if (ec != null) {
            JSONArray arr = ec.getJSONArray("word");
            JSONObject o = arr.getJSONObject(0);
            if (o != null) {
                JSONArray trs = o.getJSONArray("trs");
                for (int i = 0; i < trs.length(); i++) {
                    try {
                        d.trs.add(
                                trs.getJSONObject(i)
                                        .getJSONArray("tr").getJSONObject(0)
                                        .getJSONObject("l")
                                        .getJSONArray("i").getString(0)
                        );
                    } catch (JSONException e) {

                    }
                }

                JSONArray wfs = o.getJSONArray("wfs");
                for (int i = 0; i < wfs.length(); i++) {
                    try {
                        JSONObject wf = wfs.getJSONObject(i)
                                .getJSONObject("wf");
                        d.wfs.add(
                                new DictDetail.WF(wf.getString("name"),
                                        wf.getString("value"))
                        );

                    } catch (JSONException e) {

                    }
                }
            }


        }
    }
}