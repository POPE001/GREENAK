package com.example.greenak;

import android.widget.Filter;

import com.example.greenak.adapters.AdapterProductSeller;
import com.example.greenak.models.ModelProduct;

import java.util.ArrayList;

public class FilterProduct extends Filter
{
    private AdapterProductSeller adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProductSeller adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint)
    {
        FilterResults results = new FilterResults();
        //validate query for data search'
        if (constraint != null && constraint.length() > 0)
        {
            //search field not empty, searching something, perform search

            //change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i =0; i<filterList.size(); i++)
            {
                //chceck, search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint) )
                {
                //add filtered data to List
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }

        else
            {
                //search field empty, not searching, return original/all/complete list


                results.count = filterList.size();
                results.values = filterList;
            }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results)
    {
        adapter.productList = (ArrayList<ModelProduct>)results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
