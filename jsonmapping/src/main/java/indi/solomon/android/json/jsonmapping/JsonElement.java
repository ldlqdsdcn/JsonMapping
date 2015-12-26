/*
 * This file is copyrighted Solomon.liu all
 * Solomon , 2015
 */
package indi.solomon.android.json.jsonmapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Solomon.liu on 2015-07-14 0:04.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface JsonElement {
    /**
     * Json Object field Name
     *
     * @return
     */
    String name() default "##default";

    /**
     * this field is required
     *
     * @return
     */
    boolean requird() default false;

    /**
     * default value if jsonObject is null
     *
     * @return
     */
    String defaultValue() default "";

    /**
     * Json Object Type
     *
     * @return
     */
    Class type() default String.class;
}
