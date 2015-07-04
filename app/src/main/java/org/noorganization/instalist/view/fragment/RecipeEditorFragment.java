package org.noorganization.instalist.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import org.noorganization.instalist.R;
import org.noorganization.instalist.controller.IRecipeController;
import org.noorganization.instalist.controller.implementation.ControllerFactory;
import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.view.event.ProductSelectMessage;
import org.noorganization.instalist.view.listadapter.IngredientListAdapter;
import org.noorganization.instalist.view.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by michi on 01.07.15.
 */
public class RecipeEditorFragment extends Fragment {

    private static final String BK_EDITOR_MODE  = "editorMode";
    private static final String BK_RECIPE_ID    = "recipeId";
    private static final String BK_ADD_PRODUCTS = "productIds";
    private static final String BK_ADD_AMOUNTS  = "productAmounts";

    private static final int    EDITOR_MODE_CREATE = 1;
    private static final int    EDITOR_MODE_EDIT   = 2;

    private Recipe     mRecipe;

    private ListView mIngredients;
    private EditText mRecipeName;
    private Button   mAddIngredient;
    private Button   mSave;
    private IngredientListAdapter mIngredientAdapter;

    private ProductSelectMessage mAddToList;

    /**
     * The default constructor needed by the FragmentManager. Use the newXXXInstance for creation
     * instead.
     */
    public RecipeEditorFragment() {
    }

    public static RecipeEditorFragment newCreationInstance() {
        RecipeEditorFragment rtn = new RecipeEditorFragment();
        Bundle parameters = new Bundle();
        parameters.putInt(BK_EDITOR_MODE, EDITOR_MODE_CREATE);
        rtn.setArguments(parameters);
        return rtn;
    }

    public static RecipeEditorFragment newUpdateInstance(long _recipeId) {
        RecipeEditorFragment rtn = new RecipeEditorFragment();
        Bundle parameters = new Bundle();
        parameters.putInt(BK_EDITOR_MODE, EDITOR_MODE_EDIT);
        parameters.putLong(BK_RECIPE_ID, _recipeId);
        rtn.setArguments(parameters);
        return rtn;
    }

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);

        Bundle parameters = getArguments();
        if (parameters != null) {
            if (parameters.getInt(BK_EDITOR_MODE) == EDITOR_MODE_EDIT) {
                mRecipe = SugarRecord.findById(Recipe.class, parameters.getLong(BK_RECIPE_ID));
            }
        }

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater _inflater, ViewGroup _parent, Bundle _savedInstanceState) {
        View mainView = _inflater.inflate(R.layout.fragment_recipe_details, _parent, false);

        mIngredients = (ListView) mainView.findViewById(R.id.fragment_recipe_details_ingredients);
        View actions = _inflater.inflate(R.layout.fragment_recipe_details_actions, null);
        mIngredients.addFooterView(actions);

        mAddIngredient = (Button) actions.findViewById(R.id.fragment_recipe_details_add_ingredient);
        mSave          = (Button) actions.findViewById(R.id.fragment_recipe_details_save);
        mRecipeName    = (EditText) mainView.findViewById(R.id.fragment_recipe_details_name);

        fillViews();
        addArgIngredients();

        return mainView;
    }

    /**
     * EventBus-receiver for selections made via ProductListDialogFragment.
     */
    public void onEventBackgroundThread(ProductSelectMessage _selectedProducts) {
        long productIds[] = new long[_selectedProducts.mProducts.size()];
        float amounts[] = new float[_selectedProducts.mProducts.size()];
        int currentIndex = 0;
        for (Product currentProduct : _selectedProducts.mProducts.keySet()) {
            productIds[currentIndex] = currentProduct.getId();
            amounts[currentIndex] = _selectedProducts.mProducts.get(currentProduct);
        }
        getArguments().putLongArray(BK_ADD_PRODUCTS, productIds);
        getArguments().putFloatArray(BK_ADD_AMOUNTS, amounts);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void addArgIngredients () {
        long productIds[] = getArguments().getLongArray(BK_ADD_PRODUCTS);
        float amounts[] = getArguments().getFloatArray(BK_ADD_AMOUNTS);
        if (productIds == null || amounts == null || productIds.length != amounts.length) {
            return;
        }

        for (int currentIndex = 0; currentIndex < productIds.length; currentIndex++) {
            Ingredient toAdd = new Ingredient(SugarRecord.findById(Product.class, productIds[currentIndex]),
                    null,
                    amounts[currentIndex]);
            mIngredientAdapter.addIngredient(toAdd);
        }

        getArguments().remove(BK_ADD_AMOUNTS);
        getArguments().remove(BK_ADD_PRODUCTS);
    }

    private void fillViews() {
        List<Ingredient> ingredientList;
        if (mRecipe == null) {
            ingredientList = new ArrayList<>();
        } else {
            ingredientList = mRecipe.getIngredients();
            mRecipeName.setText(mRecipe.mName);
        }

        mIngredientAdapter = new IngredientListAdapter(getActivity(), ingredientList);
        mIngredients.setAdapter(mIngredientAdapter);

        mSave.setOnClickListener(new OnSaveListener());
        mAddIngredient.setOnClickListener(new OnAddIngredientListener());
    }

    private boolean validate() {
        boolean rtn = true;
        if (mIngredientAdapter.getIngredients().size() == 0) {
            Toast.makeText(getActivity(), "Just a test.", Toast.LENGTH_LONG).show();
            rtn = false;
        }

        String newName = mRecipeName.getText().toString();
        List<Recipe> recipesToValidate = Select.from(Recipe.class).
                where(Condition.prop(Recipe.ATTR_NAME).eq(newName)).list();
        if (mRecipeName.getText().length() == 0 ||
                (recipesToValidate.size() != 0 &&
                        (mRecipe == null || !mRecipe.getId().equals(recipesToValidate.get(0).getId())))) {
            mRecipeName.setError("Just a second test.");
            rtn = false;
        }
        return rtn;
    }

    private class OnAddIngredientListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            //ProductListDialogFragment.
            // TODO Add some action!

            ViewUtils.addFragment(getActivity(), ProductListDialogFragment.newInstance());
        }
    }

    private class OnSaveListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (validate()) {
                IRecipeController controller = ControllerFactory.getRecipeController();

                String newRecipeName = mRecipeName.getText().toString();
                if (mRecipe == null) {
                    mRecipe = controller.createRecipe(newRecipeName);
                } else {
                    mRecipe = controller.renameRecipe(mRecipe, newRecipeName);
                }

                if (mRecipe == null) {
                    Toast.makeText(getActivity(), "Something went wrong while creating recipe.", Toast.LENGTH_LONG).show();
                    return;
                }

                for (Ingredient toDelete : mIngredientAdapter.getDeleted()) {
                    controller.removeIngredient(mRecipe, toDelete.mProduct);
                }

                for (Ingredient toSave : mIngredientAdapter.getIngredients()) {
                    controller.addOrChangeIngredient(mRecipe, toSave.mProduct, toSave.mAmount);
                }

                ViewUtils.removeFragment(getActivity(), RecipeEditorFragment.this);
                getActivity().finish();
            }
        }
    }
}