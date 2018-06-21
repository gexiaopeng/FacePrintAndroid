/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp;

import com.baidu.aip.fp.exception.FaceException;

public interface OnResultListener<T> {
    void onResult(T result);

    void onError(FaceException error);
}
