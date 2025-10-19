package com.example.inventorycontrolapplication.ui.home;

import androidx.annotation.Nullable;

public class AddDataFormState {
    @Nullable
    private Integer nameError;
    @Nullable
    private Integer typeError;
    @Nullable
    private Integer countError;
    private boolean isDataValid;

    AddDataFormState(@Nullable Integer nameError, @Nullable Integer typeError, @Nullable Integer countError) {
        this.nameError = nameError;
        this.typeError = typeError;
        this.countError = countError;
        this.isDataValid = false;
    }

    AddDataFormState(boolean isDataValid) {
        this.nameError = null;
        this.typeError = null;
        this.countError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getNameError() {
        return nameError;
    }

    @Nullable
    Integer getTypeError() {
        return typeError;
    }
    @Nullable
    Integer getCountError() {
        return countError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}