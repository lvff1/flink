/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.api.utils;

import org.apache.flink.api.common.typeinfo.BasicArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.PrimitiveArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.SqlTimeTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.base.BigIntSerializer;
import org.apache.flink.api.common.typeutils.base.BooleanSerializer;
import org.apache.flink.api.common.typeutils.base.ByteSerializer;
import org.apache.flink.api.common.typeutils.base.CharSerializer;
import org.apache.flink.api.common.typeutils.base.DoubleSerializer;
import org.apache.flink.api.common.typeutils.base.FloatSerializer;
import org.apache.flink.api.common.typeutils.base.GenericArraySerializer;
import org.apache.flink.api.common.typeutils.base.IntSerializer;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.api.common.typeutils.base.ShortSerializer;
import org.apache.flink.api.common.typeutils.base.array.BytePrimitiveArraySerializer;
import org.apache.flink.api.common.typeutils.base.array.IntPrimitiveArraySerializer;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.api.java.typeutils.runtime.RowSerializer;
import org.apache.flink.api.java.typeutils.runtime.TupleSerializer;
import org.apache.flink.fnexecution.v1.FlinkFnApi;
import org.apache.flink.streaming.api.typeinfo.python.PickledByteArrayTypeInfo;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.data.util.DataFormatConverters;
import org.apache.flink.table.runtime.typeutils.InternalTypeInfo;
import org.apache.flink.table.runtime.typeutils.serializers.python.BigDecSerializer;
import org.apache.flink.table.runtime.typeutils.serializers.python.DateSerializer;
import org.apache.flink.table.runtime.typeutils.serializers.python.StringSerializer;
import org.apache.flink.table.runtime.typeutils.serializers.python.TimeSerializer;
import org.apache.flink.table.runtime.typeutils.serializers.python.TimestampSerializer;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.utils.TypeConversions;
import org.apache.flink.types.Row;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Test class for testing typeinfo to proto converter and typeinfo to type serializer converter. */
class PythonTypeUtilsTest {

    @Test
    void testTypeInfoToProtoConverter() {
        Map<TypeInformation, FlinkFnApi.TypeInfo.TypeName> typeInformationTypeNameMap =
                new HashMap<>();
        typeInformationTypeNameMap.put(
                BasicTypeInfo.INT_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.INT);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.BIG_DEC_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.BIG_DEC);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.BIG_INT_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.BIG_INT);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.CHAR_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.CHAR);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.FLOAT_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.FLOAT);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.DOUBLE_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.DOUBLE);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.SHORT_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.SHORT);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.LONG_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.LONG);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.STRING_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.STRING);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.BYTE_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.BYTE);
        typeInformationTypeNameMap.put(
                PickledByteArrayTypeInfo.PICKLED_BYTE_ARRAY_TYPE_INFO,
                FlinkFnApi.TypeInfo.TypeName.PICKLED_BYTES);
        typeInformationTypeNameMap.put(
                BasicTypeInfo.BOOLEAN_TYPE_INFO, FlinkFnApi.TypeInfo.TypeName.BOOLEAN);
        typeInformationTypeNameMap.put(SqlTimeTypeInfo.DATE, FlinkFnApi.TypeInfo.TypeName.SQL_DATE);
        typeInformationTypeNameMap.put(SqlTimeTypeInfo.TIME, FlinkFnApi.TypeInfo.TypeName.SQL_TIME);
        typeInformationTypeNameMap.put(
                SqlTimeTypeInfo.TIMESTAMP, FlinkFnApi.TypeInfo.TypeName.SQL_TIMESTAMP);

        for (Map.Entry<TypeInformation, FlinkFnApi.TypeInfo.TypeName> entry :
                typeInformationTypeNameMap.entrySet()) {
            assertThat(
                            PythonTypeUtils.TypeInfoToProtoConverter.toTypeInfoProto(
                                            entry.getKey(), null)
                                    .getTypeName())
                    .isEqualTo(entry.getValue());
        }

        TypeInformation primitiveIntegerArrayTypeInfo =
                PrimitiveArrayTypeInfo.INT_PRIMITIVE_ARRAY_TYPE_INFO;
        FlinkFnApi.TypeInfo convertedFieldType =
                PythonTypeUtils.TypeInfoToProtoConverter.toTypeInfoProto(
                        primitiveIntegerArrayTypeInfo, null);
        assertThat(convertedFieldType.getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.PRIMITIVE_ARRAY);
        assertThat(convertedFieldType.getCollectionElementType().getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.INT);

        TypeInformation basicIntegerArrayTypeInfo = BasicArrayTypeInfo.INT_ARRAY_TYPE_INFO;
        FlinkFnApi.TypeInfo convertedBasicFieldType =
                PythonTypeUtils.TypeInfoToProtoConverter.toTypeInfoProto(
                        basicIntegerArrayTypeInfo, null);
        assertThat(convertedBasicFieldType.getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.BASIC_ARRAY);
        assertThat(convertedBasicFieldType.getCollectionElementType().getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.INT);

        TypeInformation objectArrayTypeInfo = Types.OBJECT_ARRAY(Types.ROW(Types.INT));
        FlinkFnApi.TypeInfo convertedTypeInfoProto =
                PythonTypeUtils.TypeInfoToProtoConverter.toTypeInfoProto(objectArrayTypeInfo, null);
        assertThat(convertedTypeInfoProto.getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.OBJECT_ARRAY);
        assertThat(convertedTypeInfoProto.getCollectionElementType().getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.ROW);
        assertThat(
                        convertedTypeInfoProto
                                .getCollectionElementType()
                                .getRowTypeInfo()
                                .getFields(0)
                                .getFieldType()
                                .getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.INT);

        TypeInformation rowTypeInfo = Types.ROW(Types.INT);
        convertedFieldType =
                PythonTypeUtils.TypeInfoToProtoConverter.toTypeInfoProto(rowTypeInfo, null);
        assertThat(convertedFieldType.getTypeName()).isEqualTo(FlinkFnApi.TypeInfo.TypeName.ROW);
        assertThat(convertedFieldType.getRowTypeInfo().getFields(0).getFieldType().getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.INT);

        TypeInformation tupleTypeInfo = Types.TUPLE(Types.INT);
        convertedFieldType =
                PythonTypeUtils.TypeInfoToProtoConverter.toTypeInfoProto(tupleTypeInfo, null);
        assertThat(convertedFieldType.getTypeName()).isEqualTo(FlinkFnApi.TypeInfo.TypeName.TUPLE);
        assertThat(convertedFieldType.getTupleTypeInfo().getFieldTypes(0).getTypeName())
                .isEqualTo(FlinkFnApi.TypeInfo.TypeName.INT);
    }

    @Test
    void testTypeInfoToSerializerConverter() {
        Map<TypeInformation, TypeSerializer> typeInformationTypeSerializerMap = new HashMap<>();
        typeInformationTypeSerializerMap.put(BasicTypeInfo.INT_TYPE_INFO, IntSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                BasicTypeInfo.BIG_DEC_TYPE_INFO, BigDecSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                BasicTypeInfo.BIG_INT_TYPE_INFO, BigIntSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(BasicTypeInfo.CHAR_TYPE_INFO, CharSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                BasicTypeInfo.FLOAT_TYPE_INFO, FloatSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                BasicTypeInfo.DOUBLE_TYPE_INFO, DoubleSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                BasicTypeInfo.SHORT_TYPE_INFO, ShortSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(BasicTypeInfo.LONG_TYPE_INFO, LongSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                BasicTypeInfo.STRING_TYPE_INFO, StringSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(BasicTypeInfo.BYTE_TYPE_INFO, ByteSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                PickledByteArrayTypeInfo.PICKLED_BYTE_ARRAY_TYPE_INFO,
                BytePrimitiveArraySerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(
                BasicTypeInfo.BOOLEAN_TYPE_INFO, BooleanSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(SqlTimeTypeInfo.DATE, DateSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(SqlTimeTypeInfo.TIME, TimeSerializer.INSTANCE);
        typeInformationTypeSerializerMap.put(SqlTimeTypeInfo.TIMESTAMP, new TimestampSerializer(3));

        for (Map.Entry<TypeInformation, TypeSerializer> entry :
                typeInformationTypeSerializerMap.entrySet()) {
            assertThat(entry.getValue())
                    .isEqualTo(
                            PythonTypeUtils.TypeInfoToSerializerConverter
                                    .typeInfoSerializerConverter(entry.getKey()));
        }

        TypeInformation primitiveIntegerArrayTypeInfo =
                PrimitiveArrayTypeInfo.INT_PRIMITIVE_ARRAY_TYPE_INFO;
        TypeSerializer convertedTypeSerializer =
                PythonTypeUtils.TypeInfoToSerializerConverter.typeInfoSerializerConverter(
                        primitiveIntegerArrayTypeInfo);
        assertThat(convertedTypeSerializer).isEqualTo(IntPrimitiveArraySerializer.INSTANCE);

        TypeInformation integerArrayTypeInfo = BasicArrayTypeInfo.INT_ARRAY_TYPE_INFO;
        convertedTypeSerializer =
                PythonTypeUtils.TypeInfoToSerializerConverter.typeInfoSerializerConverter(
                        integerArrayTypeInfo);
        assertThat(convertedTypeSerializer)
                .isEqualTo(new GenericArraySerializer(Integer.class, IntSerializer.INSTANCE));

        TypeInformation objectArrayTypeInfo = Types.OBJECT_ARRAY(Types.ROW(Types.INT));
        convertedTypeSerializer =
                PythonTypeUtils.TypeInfoToSerializerConverter.typeInfoSerializerConverter(
                        objectArrayTypeInfo);
        assertThat(convertedTypeSerializer)
                .isEqualTo(
                        new GenericArraySerializer(
                                Row.class,
                                new RowSerializer(
                                        new TypeSerializer[] {IntSerializer.INSTANCE}, null)));

        TypeInformation rowTypeInfo = Types.ROW(Types.INT);
        convertedTypeSerializer =
                PythonTypeUtils.TypeInfoToSerializerConverter.typeInfoSerializerConverter(
                        rowTypeInfo);
        assertThat(convertedTypeSerializer)
                .isEqualTo(new RowSerializer(new TypeSerializer[] {IntSerializer.INSTANCE}, null));

        TupleTypeInfo tupleTypeInfo = (TupleTypeInfo) Types.TUPLE(Types.INT);
        convertedTypeSerializer =
                PythonTypeUtils.TypeInfoToSerializerConverter.typeInfoSerializerConverter(
                        tupleTypeInfo);
        assertThat(convertedTypeSerializer)
                .isEqualTo(
                        new TupleSerializer(
                                tupleTypeInfo.getTypeClass(),
                                new TypeSerializer[] {IntSerializer.INSTANCE}));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testInternalTypeInfoToDataConverter() {
        RowType rowDataType =
                (RowType)
                        DataTypes.ROW(
                                        DataTypes.BOOLEAN(),
                                        DataTypes.TINYINT(),
                                        DataTypes.SMALLINT(),
                                        DataTypes.INT(),
                                        DataTypes.BIGINT(),
                                        DataTypes.FLOAT(),
                                        DataTypes.DOUBLE(),
                                        DataTypes.BINARY(10),
                                        DataTypes.VARCHAR(100),
                                        DataTypes.CHAR(100),
                                        DataTypes.VARCHAR(1000),
                                        DataTypes.DATE(),
                                        DataTypes.TIME(),
                                        DataTypes.ARRAY(DataTypes.STRING()),
                                        DataTypes.MAP(DataTypes.BIGINT(), DataTypes.BYTES()))
                                .getLogicalType();
        PythonTypeUtils.DataConverter dataConverter =
                PythonTypeUtils.TypeInfoToDataConverter.typeInfoDataConverter(
                        InternalTypeInfo.of(rowDataType));

        PythonTypeUtils.RowDataConverter rowDataConverter =
                new PythonTypeUtils.RowDataConverter(
                        new PythonTypeUtils.DataConverter[] {
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.ByteDataConverter.INSTANCE,
                            PythonTypeUtils.ShortDataConverter.INSTANCE,
                            PythonTypeUtils.IntDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.FloatDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            PythonTypeUtils.IdentityDataConverter.INSTANCE,
                            new PythonTypeUtils.ArrayDataConverter<>(
                                    String.class, PythonTypeUtils.IdentityDataConverter.INSTANCE),
                            new PythonTypeUtils.MapDataConverter(
                                    PythonTypeUtils.IdentityDataConverter.INSTANCE,
                                    PythonTypeUtils.IdentityDataConverter.INSTANCE)
                        });

        PythonTypeUtils.RowDataDataConverter expectedDataConverter =
                new PythonTypeUtils.RowDataDataConverter(
                        rowDataConverter,
                        DataFormatConverters.getConverterForDataType(
                                TypeConversions.fromLogicalToDataType(rowDataType)));

        assertThat(dataConverter).isEqualTo(expectedDataConverter);
    }
}
