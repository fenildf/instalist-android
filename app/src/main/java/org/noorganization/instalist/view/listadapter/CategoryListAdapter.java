package org.noorganization.instalist.view.listadapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.noorganization.instalist.model.Category;

import java.util.List;

/**
 * Created by TS on 21.06.2015.
 */
public class CategoryListAdapter extends ArrayAdapter<Category> {

    private List<Category> mCategoryList;
    private Activity       mContext;

    public CategoryListAdapter(Activity _Context, List<Category> _CategoryList) {
        super(_Context, android.R.layout.simple_spinner_dropdown_item, _CategoryList);
        mCategoryList = _CategoryList;
        mContext = _Context;
    }

    @Override
    public View getView(int _Position, View _ConvertView, ViewGroup _Parent) {
        View     view            = null;
        Category ingredientEntry = mCategoryList.get(_Position);

        if (_ConvertView == null) {
            LayoutInflater shoppingListNamesInflater = mContext.getLayoutInflater();
            view = shoppingListNamesInflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
        } else {
            view = _ConvertView;
        }

        TextView categoryName = (TextView) view.findViewById(android.R.id.text1);

        Category category = mCategoryList.get(_Position);

        categoryName.setText(category.mName);
        return view;
    }
}