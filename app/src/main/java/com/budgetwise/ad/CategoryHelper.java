// CategoryHelper.java
package com.budgetwise.ad;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.List;

public class CategoryHelper {
    public static void loadCategoriesIntoSpinner(Context context, Spinner spinner) {
        CategoryDAO categoryDAO = new CategoryDAO(context);
        List<Category> categories = categoryDAO.getAllActiveCategories();

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void selectSpinnerValue(Spinner spinner, String categoryId) {
        SpinnerAdapter adapter = spinner.getAdapter();
        if (adapter == null || categoryId == null) {
            return;
        }
        for (int position = 0; position < adapter.getCount(); position++) {
            Object item = adapter.getItem(position);
            if (item instanceof Category) {
                Category category = (Category) item;
                if (categoryId.equals(category.getCategoryId())) {
                    spinner.setSelection(position);
                    break;
                }
            }
        }
    }
    public static Category getCategoryById(Context context, String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) return null;

        CategoryDAO dao = new CategoryDAO(context);
        // Giả sử bạn có method này trong CategoryDAO
        // Nếu chưa có thì mình sẽ tặng luôn ở dưới
        return dao.getCategoryById(categoryId);
    }
    /**
     * Lấy tên danh mục (tránh NPE, trả về "Không xác định" nếu không tìm thấy)
     * Dùng gọn trong Adapter
     */
    public static String getCategoryNameById(Context context, String categoryId) {
        Category cat = getCategoryById(context, categoryId);
        return cat != null ? cat.getName() : "Không xác định";
    }
}