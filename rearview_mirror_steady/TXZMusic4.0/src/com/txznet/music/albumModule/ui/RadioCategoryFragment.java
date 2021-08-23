package com.txznet.music.albumModule.ui;

import com.txznet.music.data.entity.Category;
import com.txznet.music.report.ReportEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brainBear on 2018/2/24.
 */

public class RadioCategoryFragment extends CategoryFragment {
    @Override
    public List<Category> onCategoriesFilter(List<Category> mCategories) {

        List<Category> categories = new ArrayList<Category>();
        for (Category category : mCategories) {
            if (category.getCategoryId() != 100000 && category.getCategoryId() != 1) {
                categories.add(category);
            }
        }
        return categories;
    }

    @Override
    public void onTabSelected(Category category) {
        ReportEvent.clickRadioCategory(category.getCategoryId());
    }
}
