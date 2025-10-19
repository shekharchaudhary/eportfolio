package com.example.inventorycontrolapplication.ui.home;

import androidx.annotation.Nullable;

public class EditDataFormState {
    @Nullable
    private Integer countError;
    private boolean isDataValid;

    EditDataFormState( @Nullable Integer countError) {
        this.countError = countError;
        this.isDataValid = false;
    }

    EditDataFormState(boolean isDataValid) {
        this.countError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getCountError() {
        return countError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}