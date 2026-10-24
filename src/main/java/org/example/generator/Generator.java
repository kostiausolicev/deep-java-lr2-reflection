package org.example.generator;

import org.example.Generatable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Generator {
    private final Random random = new Random();
    private final Map<Class<?>, List<Class<?>>> implementations = new HashMap<>();

    @SafeVarargs
    public final <T> Generator registerImplementations(
            Class<T> abstraction,
            Class<? extends T>... implementationClasses
    ) {
        if (!abstraction.isInterface() && !Modifier.isAbstract(abstraction.getModifiers())) {
            throw new IllegalArgumentException(
                    "Implementation registration is only supported for interfaces and abstract classes: "
                            + abstraction.getName()
            );
        }
        if (implementationClasses.length == 0) {
            throw new IllegalArgumentException(
                    "At least one implementation must be registered for " + abstraction.getName()
            );
        }

        for (Class<? extends T> implementationClass : implementationClasses) {
            if (implementationClass.isInterface()
                    || Modifier.isAbstract(implementationClass.getModifiers())) {
                throw new IllegalArgumentException(
                        "Registered implementation must be a concrete class: "
                                + implementationClass.getName()
                );
            }
        }

        implementations.put(abstraction, List.of(implementationClasses));
        return this;
    }

    public Object generateValueOfType(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object simple = generateSimpleType(clazz);
        if (simple != null) return simple;
        Object _enum = generateEnum(clazz);
        if (_enum != null) return _enum;
        Object array = generateArray(clazz);
        if (array != null) return array;
        Object list = generateList(clazz);
        if (list != null) return list;
        Object map = generateMap(clazz);
        if (map != null) return map;

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> constructor = getGeneratableConstructor(constructors);
        if (constructor == null) {
            constructor = getRandomConstructor(constructors);
        }

        Type[] argumentTypes = constructor.getGenericParameterTypes();
        if (argumentTypes.length == 0) {
            // return without allocations
            return constructor.newInstance();
        }
        Object[] arguments = new Object[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            arguments[i] = generateValueOfType(argumentTypes[i]);
        }

        return constructor.newInstance(arguments);
    }

    private Object generateValueOfType(Type type) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (type instanceof Class<?> clazz) {
            return generateValueOfType(clazz);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (rawType == List.class && typeArguments.length == 1) {
                return generateList(typeArguments[0]);
            }
            if (rawType == Map.class && typeArguments.length == 2) {
                return generateMap(typeArguments[0], typeArguments[1]);
            }
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getTypeName());
    }

    private Object generateSimpleType(Class<?> clazz) {
        if (clazz == int.class || clazz == Integer.class) {
            return random.nextInt();
        }
        if (clazz == long.class || clazz == Long.class) {
            return random.nextLong();
        }
        if (clazz == float.class || clazz == Float.class) {
            return random.nextFloat();
        }
        if (clazz == double.class || clazz == Double.class) {
            return random.nextDouble();
        }
        if (clazz == char.class || clazz == Character.class) {
            return (char) random.nextInt('a', 'z' + 1);
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            return random.nextBoolean();
        }
        if (clazz == short.class || clazz == Short.class) {
            return (short) random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1);
        }
        if (clazz == byte.class || clazz == Byte.class) {
            return (byte) random.nextInt();
        }

        if (clazz == String.class) {
            int len = random.nextInt(5, 20);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append((char) random.nextInt('a', 'z' + 1));
            }
            return sb.toString();
        }

        return null;
    }

    private Object generateEnum(Class<?> clazz) {
        if (!clazz.isEnum()) return null;
        Object[] constants = clazz.getEnumConstants();
        int index = random.nextInt(constants.length);
        return constants[index];
    }

    private Object generateArray(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        if (!clazz.isArray()) {
            return null;
        }

        Class<?> componentType = clazz.getComponentType();
        int length = random.nextInt(1, 5);

        Object array = Array.newInstance(componentType, length);

        for (int i = 0; i < length; i++) {
            Object value = generateValueOfType(componentType);
            Array.set(array, i, value);
        }

        return array;
    }

    private Object generateList(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (clazz != List.class) {
            return null;
        }

        int size = random.nextInt(1, 5);
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(generateValueOfType(String.class));
        }

        return list;
    }

    private List<Object> generateList(Type elementType) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int size = random.nextInt(1, 5);
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(generateValueOfType(elementType));
        }
        return list;
    }

    private Object generateMap(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (clazz != Map.class) {
            return null;
        }

        int size = random.nextInt(1, 5);
        Map<Object, Object> map = new HashMap<>(size);
        while (map.size() < size) {
            Object key = generateValueOfType(String.class);
            Object value = generateValueOfType(Integer.class);
            map.put(key, value);
        }

        return map;
    }

    private Map<Object, Object> generateMap(Type keyType, Type valueType) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int size = random.nextInt(1, 5);
        Map<Object, Object> map = new HashMap<>(size);
        while (map.size() < size) {
            Object key = generateValueOfType(keyType);
            Object value = generateValueOfType(valueType);
            map.put(key, value);
        }
        return map;
    }

    private Constructor<?> getRandomConstructor(Constructor<?>[] constructors) {
        int randomConstructorIndex = random.nextInt(constructors.length);
        return constructors[randomConstructorIndex];
    }

    private Constructor<?> getGeneratableConstructor(Constructor<?>[] constructors) {
        List<Constructor<?>> generatableConstructors = Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Generatable.class))
                .toList();
        if (generatableConstructors.size() > 1) {
            throw new IllegalStateException("Class must not have more than one @Generatable constructor");
        }

        return generatableConstructors.stream().findFirst().orElse(null);
    }
}
