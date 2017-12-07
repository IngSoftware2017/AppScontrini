package com.unipd.ingsw.gruppo3;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import database.PersonEntity;

/**
 * Created by Federico Taschin on 06/12/2017.
 */

public class AddPersonEditText extends AppCompatEditText {
    PersonEntity personEntity;

    public AddPersonEditText(Context context) {
        super(context);
    }

    public AddPersonEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddPersonEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PersonEntity getPersonEntity() {
        return personEntity;
    }

    public void setPersonEntity(PersonEntity personEntity) {
        this.personEntity = personEntity;
    }
}
