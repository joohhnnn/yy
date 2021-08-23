package com.txznet.music.albumModule.ui;

import com.txznet.music.data.entity.Category;
import com.txznet.music.report.ReportEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brainBear on 2018/2/24.
 */

public class MusicCategoryFragment extends CategoryFragment {

    @Override
    public List<Category> onCategoriesFilter(List<Category> categories) {

        List<Category> newCategories = new ArrayList<Category>();
        for (Category category : categories) {
            if (category.getCategoryId() == 100000) {
                newCategories.addAll(category.getArrChild());
            }
        }
        return newCategories;
    }

    @Override
    public void onTabSelected(Category category) {
        ReportEvent.clickMusicCategory(category.getCategoryId());
    }
}
