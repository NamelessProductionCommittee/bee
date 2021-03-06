/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.util.lambda;

import java.util.function.Supplier;

/**
 * 
 */
public interface ReflectableSupplier<T> extends Supplier<T>, Reflectable {
}