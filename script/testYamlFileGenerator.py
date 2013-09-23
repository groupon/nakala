#!/usr/bin/env python

from generate_yaml_file import *
import unittest

class TestYamlFileGenerator(unittest.TestCase):

    def setUp(self):
        self.maxDiff = None
        self.generator = YamlFileGenerator()

    def test_validate_lengths_equal(self):
        self.generator.parameters = {SVM_OUTPUT_STEM:['a','b','c'], FEATURE_FILE:['d','e','f']}
        errors = []
        self.assertTrue(self.generator._validate_list_lengths_equal(SVM_OUTPUT_STEM, FEATURE_FILE, errors))
        self.assertEquals(0, len(errors))

        self.generator.parameters = {SVM_OUTPUT_STEM:['a','b','c'], FEATURE_FILE:['d','e']}
        errors = []
        self.assertFalse(self.generator._validate_list_lengths_equal(SVM_OUTPUT_STEM, FEATURE_FILE, errors))
        self.assertEquals(1, len(errors))

    def test_validate_length_equals(self):
        self.generator.parameters = {FEATURE_FILE:['d']}
        errors = []
        self.assertTrue(self.generator._validate_length_equals(FEATURE_FILE, 1, errors))
        self.assertEquals(0, len(errors))
        errors = []
        self.assertFalse(self.generator._validate_length_equals(FEATURE_FILE, 2, errors))
        self.assertEquals(1, len(errors))

    def test_validate_presence_of(self):
        self.generator.parameters = {SVM_OUTPUT_STEM:['a','b','c'], FEATURE_FILE:['d','e','f']}
        keys = [SVM_OUTPUT_STEM, FEATURE_FILE]
        errors = []
        used_keys = set()
        self.assertTrue(self.generator._validate_presence_of(keys, errors, used_keys))
        self.assertEquals(set(keys), used_keys)
        self.assertEquals(0, len(errors))
    
    def test_validate_optional_params(self):
        self.generator.parameters = {SVM_OUTPUT_STEM:['a','b','c'], FEATURE_FILE:['d','e','f']}
        keys = [SVM_OUTPUT_STEM, FEATURE_FILE]
        used_keys = set()        
        self.generator._validate_optional_params(keys, used_keys)
        self.assertEquals(set(keys), used_keys)

        keys = [SVM_OUTPUT_STEM, QUIET_MODEL]
        used_keys = set()
        self.generator._validate_optional_params(keys, used_keys)
        self.assertEquals(set([SVM_OUTPUT_STEM]), used_keys)
    
    def test_validate_quiet_feature(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        label_field = 2
        data_store = 'output.file'
        target_class = 'target class'
        max_feature_size = 10000
        min_feature_weight = 0.01

        # Testing required parameters
        self.generator.parameters = {TASK:QUIET, PHASE:FEATURE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, DATA_STORE: data_store}
        self.assertTrue(self.generator._validate())
        
        # Testing with optional parameters
        self.generator.parameters = {TASK:QUIET, PHASE:FEATURE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, DATA_STORE: data_store,
                                     TARGET_CLASS: target_class, MAX_FEATURE_SIZE: max_feature_size,
                                     MIN_FEATURE_WEIGHT: min_feature_weight}
        self.assertTrue(self.generator._validate())

    def test_validate_svm_feature(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        label_field = 2
        data_store = 'output.file'
        target_class = 'target class'
        max_feature_size = 10000
        min_feature_weight = 0.01

        # Testing required parameters
        self.generator.parameters = {TASK:SVM, PHASE:FEATURE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, DATA_STORE: data_store}
        self.assertTrue(self.generator._validate())
        
        # Testing with optional parameters
        self.generator.parameters = {TASK:SVM, PHASE:FEATURE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, DATA_STORE: data_store,
                                     TARGET_CLASS: target_class, MAX_FEATURE_SIZE: max_feature_size,
                                     MIN_FEATURE_WEIGHT: min_feature_weight}
        self.assertTrue(self.generator._validate())

    def test_validate_quiet_learn(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        label_field = 2
        feature_file = ['features.tsv']
        data_store = 'output.file'
        target_class = 'target class'
        number_of_threads = 20

        # Testing required parameters
        self.generator.parameters = {TASK:QUIET, PHASE:LEARN, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, FEATURE_FILE: feature_file,
                                     DATA_STORE: data_store, TARGET_CLASS: target_class}
        self.assertTrue(self.generator._validate())
        
        # Testing with optional parameters
        self.generator.parameters = {TASK:QUIET, PHASE:LEARN, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, FEATURE_FILE: feature_file, DATA_STORE: data_store,
                                     TARGET_CLASS: target_class, NUMBER_OF_THREADS: number_of_threads}
        self.assertTrue(self.generator._validate())

    def test_validate_svm_learn(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        label_field = 2
        feature_file = ['features.tsv']
        data_store = 'output.file'
        target_class = 'target class'
        number_of_threads = 20
        sample = 0.3

        # Testing required parameters
        self.generator.parameters = {TASK:SVM, PHASE:LEARN, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, FEATURE_FILE: feature_file, DATA_STORE: data_store}
        self.assertTrue(self.generator._validate())
        
        # Testing with optional parameters
        self.generator.parameters = {TASK:SVM, PHASE:LEARN, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, FEATURE_FILE: feature_file, DATA_STORE: data_store,
                                     TARGET_CLASS: target_class, NUMBER_OF_THREADS: number_of_threads, SAMPLE: sample}
        self.assertTrue(self.generator._validate())

    def test_validate_quiet_evaluate(self):
        input_file = 'test_data.tsv'
        id_field = 0
        text_field = 1
        label_field = 2
        quiet_model = ['quiet_model']
        data_store = 'output.file'
        target_class = 'target class'

        # Testing required parameters
        self.generator.parameters = {TASK:QUIET, PHASE:EVALUATE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, QUIET_MODEL:quiet_model, DATA_STORE: data_store}
        self.assertTrue(self.generator._validate())

        # Testing with optional parameters
        self.generator.parameters = {TASK:QUIET, PHASE:EVALUATE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, QUIET_MODEL: quiet_model, DATA_STORE: data_store,
                                     TARGET_CLASS: target_class}
        self.assertTrue(self.generator._validate())

    def test_validate_svm_evaluate(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        label_field = 2
        feature_file = ['features.tsv']
        svm_output_stem = ['svm_stem']
        data_store = 'output.file'
        target_class = 'target class'

        # Testing required parameters
        self.generator.parameters = {TASK:SVM, PHASE:EVALUATE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, FEATURE_FILE: feature_file, SVM_OUTPUT_STEM: svm_output_stem, DATA_STORE: data_store}
        self.assertTrue(self.generator._validate())
        
        # Testing with optional parameters
        self.generator.parameters = {TASK:SVM, PHASE:EVALUATE, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     LABEL_FIELD: label_field, FEATURE_FILE: feature_file, SVM_OUTPUT_STEM: svm_output_stem, DATA_STORE: data_store,
                                     TARGET_CLASS: target_class}
        self.assertTrue(self.generator._validate())

    def test_validate_quiet_classify(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        quiet_model = ['model1', 'model2', 'model3']
        data_store = 'output.file'
        number_of_threads = 20

        # Testing required parameters
        self.generator.parameters = {TASK:QUIET, PHASE:CLASSIFY, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     QUIET_MODEL: quiet_model, DATA_STORE: data_store}
        self.assertTrue(self.generator._validate())
        
        # Testing with optional parameters
        self.generator.parameters = {TASK:QUIET, PHASE:CLASSIFY, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     QUIET_MODEL: quiet_model, DATA_STORE: data_store,
                                     NUMBER_OF_THREADS: number_of_threads}
        self.assertTrue(self.generator._validate())

    def test_validate_svm_classify(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        feature_file = ['features1.tsv', 'features2.tsv']
        svm_output_stem = ['svm_stem1', 'svm_stem2']
        data_store = 'output.file'
        number_of_threads = 20

        # Testing required parameters
        self.generator.parameters = {TASK:SVM, PHASE:CLASSIFY, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     FEATURE_FILE: feature_file, SVM_OUTPUT_STEM: svm_output_stem, DATA_STORE: data_store}
        self.assertTrue(self.generator._validate())
        
        # Testing with optional parameters
        self.generator.parameters = {TASK:SVM, PHASE:CLASSIFY, INPUT_FILE: input_file,
                                     ID_FIELD: id_field, TEXT_FIELD: text_field,
                                     FEATURE_FILE: feature_file, SVM_OUTPUT_STEM: svm_output_stem, DATA_STORE: data_store,
                                     NUMBER_OF_THREADS: number_of_threads}
        self.assertTrue(self.generator._validate())

    def test_getConfiguration(self):
        input_file = 'training.tsv'
        id_field = 0
        text_field = 1
        label_field = 2
        data_store = 'output.file'

        parameters = {TASK:SVM, PHASE:FEATURE, INPUT_FILE: input_file, ID_FIELD: id_field,
                      TEXT_FIELD: text_field, LABEL_FIELD: label_field, DATA_STORE: data_store}
        config = self.generator.getConfiguration(parameters)
        self.assertTrue(config.has_key(COLLECTION_READER))
        self.assertTrue(config.has_key(COLLECTION_ANALYZER))
        self.assertTrue(config.has_key(DATA_STORES))

    def test_get_learner_config(self):
        feature_file=["features.tsv"]
        self.generator.parameters = {TASK:SVM, FEATURE_FILE:feature_file}
        self.assertEquals("com.groupon.ml.svm.LibSvmTrainer", self.generator._get_learner_config()['class_name'])

        self.generator.parameters = {TASK:QUIET, FEATURE_FILE:feature_file}
        self.assertEquals("com.groupon.ml.quiet.QueryExtractorCollectionAnalyzer", self.generator._get_learner_config()['class_name'])
    
    def test_get_evaluate_config(self):
        svm_output_stem = ["svm_output"]
        feature_file = ["feature.tsv"]
        self.generator.parameters = {TASK:SVM, SVM_OUTPUT_STEM:svm_output_stem, FEATURE_FILE:feature_file}
        config = self.generator._get_evaluate_config()
        self.assertEquals("com.groupon.ml.svm.LibSvmTextClassifier", config['parameters']['analyzer']['class_name'])

        quiet_model = ["model.bin"]
        self.generator.parameters = {TASK:QUIET, QUIET_MODEL:quiet_model}
        config = self.generator._get_evaluate_config()
        self.assertEquals("com.groupon.ml.quiet.HummingBirdAnalyzer", config['parameters']['analyzer']['class_name'])
    
    def test_get_batch_classifier_config(self):
        number_of_threads = 20
        feature_file = 'file1 file2 file3 file4'.split()
        svm_output_stem = 'stem1 stem2 stem3 stem4'.split()
        self.generator.parameters = {TASK:SVM, NUMBER_OF_THREADS:number_of_threads, SVM_OUTPUT_STEM: svm_output_stem, FEATURE_FILE: feature_file}
        config = self.generator._get_batch_classifier_config()
        self.assertEquals("com.groupon.ml.ClassifierCollectionAnalyzer", config['class_name'])
        self.assertEquals("com.groupon.ml.svm.MultiModelClassifier", config['parameters']['analyzer']['class_name'])
        self.assertEquals(number_of_threads, config['parameters']['analyzer']['parameters']['number_of_threads'])
        self.assertEquals(len(feature_file), len(config['parameters']['analyzer']['parameters']['analyzers']))
        self.assertEquals("com.groupon.ml.svm.LibSvmTextClassifier", config['parameters']['analyzer']['parameters']['analyzers'][0]['class_name'])
        
        quiet_model = 'model1 model2 model3'.split()
        self.generator.parameters = {TASK:QUIET, NUMBER_OF_THREADS:number_of_threads, QUIET_MODEL: quiet_model}
        config = self.generator._get_batch_classifier_config()
        self.assertEquals(len(quiet_model), len(config['parameters']['models']))
        self.assertEquals("com.groupon.ml.quiet.QuietCollectionAnalyzer", config['class_name'])
    
    def test_get_quiet_models_config(self):
        quiet_model = ['a','b','c','d']
        self.generator.parameters = {QUIET_MODEL: quiet_model}
        models = self.generator._get_quiet_models_config()
        for i in xrange(len(quiet_model)):
            self.assertEquals(quiet_model[i], models[i]['parameters']['file_name'])
    
    def test_get_svm_analyzers_config(self):
        svm_output_stem = ["svm1","svm2","svm3"]
        feature_file = ["f1","f2","f3"]
        self.generator.parameters = {SVM_OUTPUT_STEM: svm_output_stem, FEATURE_FILE: feature_file}
        analyzers = self.generator._get_svm_analyzers_config()
        for i in xrange(len(svm_output_stem)):
            self.assertEquals(svm_output_stem[i] + ".model", analyzers[i]['parameters']['model']['parameters']['file_name'])
            self.assertEquals(feature_file[i], analyzers[i]['parameters']['representer']['parameters']['features']['parameters']['file_name'])

    def test_get_quiet_model_config(self):
        quiet_model = ["model.bin"]
        expected = dict(class_name="com.groupon.ml.quiet.HummingBirdModelLoader",
                        parameters=dict(file_name=quiet_model[0]))
        self.assertEquals(expected, self.generator._get_quiet_model_config(quiet_model[0]))

    def test_get_quiet_evaluation_config(self):
        quiet_model = ["model.bin"]
        number_of_threads = 40
        self.generator.parameters = {QUIET_MODEL:quiet_model}
        expected = dict(class_name="com.groupon.ml.ClassifierEvaluator",
                        parameters=dict(min_threshold=0.005,
                                        max_threshold=1.0,
                                        threshold_step=0.005,
                                        analyzer=dict(class_name="com.groupon.ml.quiet.HummingBirdAnalyzer",
                                                      parameters=dict(model=dict(class_name="com.groupon.ml.quiet.HummingBirdModelLoader",
                                                                                 parameters=dict(file_name=quiet_model[0]))))))
        self.assertEquals(expected, self.generator._get_quiet_evaluation_config())        
    
    def test_get_svm_analyzer_config(self):
        svm_output_stem = ["svm_output"]
        feature_file = ["feature.tsv"]
        self.generator.parameters = {SVM_OUTPUT_STEM:svm_output_stem, FEATURE_FILE:feature_file}
        expected = dict(class_name="com.groupon.ml.svm.LibSvmTextClassifier",
                                                      parameters=dict(model=dict(class_name="com.groupon.nakala.core.ResourceReader",
                                                                                 parameters=dict(file_name="%s.model" %svm_output_stem[0])),
                                                                      labels=dict(class_name="com.groupon.nakala.core.ResourceReader",
                                                                                  parameters=dict(file_name="%s.labels" %svm_output_stem[0])),
                                                                      representer=dict(class_name="com.groupon.nakala.core.TfFeatureWeightTextRepresenter",
                                                                                       parameters=dict(normalize_by_length=True,
                                                                                                       features=dict(class_name="com.groupon.nakala.core.Features",
                                                                                                                     parameters=dict(file_name=feature_file[0])),
                                                                                                       tokenizer=dict(class_name="com.groupon.nakala.core.RegexpTokenizerStream"),
                                                                                                       normalizers=[{'class_name':'com.groupon.nakala.normalization.MarkupRemover'},
                                                                                                        {'class_name':'com.groupon.nakala.normalization.CaseNormalizer'},
                                                                                                        {'class_name':'com.groupon.nakala.normalization.NumberNormalizer'}],
                                                                                                       scaler=dict(class_name="com.groupon.ml.svm.ValueScaler",
                                                                                                                   parameters=dict(file_name="%s.range" %svm_output_stem[0]))))))
        self.assertEquals(expected, self.generator._get_svm_analyzer_config(feature_file[0], svm_output_stem[0]))
    
    def test_get_svm_evaluation_config(self):
        svm_output_stem = ["svm_output"]
        feature_file = ["feature.tsv"]
        self.generator.parameters = {SVM_OUTPUT_STEM:svm_output_stem, FEATURE_FILE:feature_file}
        expected = dict(class_name="com.groupon.ml.ClassifierEvaluator",
                        parameters=dict(min_threshold=0.05,
                                        max_threshold=1.0,
                                        threshold_step=0.05,
                                        analyzer=dict(class_name="com.groupon.ml.svm.LibSvmTextClassifier",
                                                      parameters=dict(model=dict(class_name="com.groupon.nakala.core.ResourceReader",
                                                                                 parameters=dict(file_name="%s.model" %svm_output_stem[0])),
                                                                      labels=dict(class_name="com.groupon.nakala.core.ResourceReader",
                                                                                  parameters=dict(file_name="%s.labels" %svm_output_stem[0])),
                                                                      representer=dict(class_name="com.groupon.nakala.core.TfFeatureWeightTextRepresenter",
                                                                                       parameters=dict(normalize_by_length=True,
                                                                                                       features=dict(class_name="com.groupon.nakala.core.Features",
                                                                                                                     parameters=dict(file_name=feature_file[0])),
                                                                                                       tokenizer=dict(class_name="com.groupon.nakala.core.RegexpTokenizerStream"),
                                                                                                       normalizers=[{'class_name':'com.groupon.nakala.normalization.MarkupRemover'},
                                                                                                        {'class_name':'com.groupon.nakala.normalization.CaseNormalizer'},
                                                                                                        {'class_name':'com.groupon.nakala.normalization.NumberNormalizer'}],
                                                                                                       scaler=dict(class_name="com.groupon.ml.svm.ValueScaler",
                                                                                                                   parameters=dict(file_name="%s.range" %svm_output_stem[0]))))))))
        self.assertEquals(expected, self.generator._get_svm_evaluation_config())
        
        target_class = "test_class"
        self.generator.parameters[TARGET_CLASS] = target_class
        expected['parameters'][TARGET_CLASS] = target_class
        self.assertEquals(expected, self.generator._get_svm_evaluation_config())        
    
    def test_get_data_stores_config(self):
        data_store="output_file.tsv"
        self.generator.parameters = {TASK:SVM, PHASE:LEARN, DATA_STORE:data_store}
        expected = [dict(class_name="com.groupon.nakala.db.FlatFileStore", parameters=dict(file_name=data_store))]
        self.assertEquals(expected, self.generator._get_data_stores_config())

        self.generator.parameters = {TASK:QUIET, PHASE:LEARN, DATA_STORE:data_store}
        expected = [dict(class_name="com.groupon.nakala.db.SerializationStore", parameters=dict(file_name=data_store))]
        self.assertEquals(expected, self.generator._get_data_stores_config())
    
    def test_get_svm_learner_config_default(self):
        feature_file=["features.tsv"]
        self.generator.parameters = {TASK:SVM, FEATURE_FILE:feature_file}
        expected = dict(class_name="com.groupon.ml.svm.LibSvmTrainer",
                        parameters=dict(find_best_parameters=True,
                                        representer=dict(class_name="com.groupon.nakala.core.TfFeatureWeightTextRepresenter",
                                                         parameters=dict(normalize_by_length=True,
                                                                         features=dict(class_name="com.groupon.nakala.core.Features",
                                                                                       parameters=dict(file_name=feature_file[0])),
                                                                         tokenizer=dict(class_name="com.groupon.nakala.core.RegexpTokenizerStream"),
                                                                         normalizers=[{'class_name':'com.groupon.nakala.normalization.MarkupRemover'},
                                                                                      {'class_name':'com.groupon.nakala.normalization.CaseNormalizer'},
                                                                                      {'class_name':'com.groupon.nakala.normalization.NumberNormalizer'}]))))
        self.assertEquals(expected, self.generator._get_svm_learner_config())
    
    def test_get_svm_learner_config(self):
        feature_file=["features.tsv"]
        sample=0.2
        number_of_threads = 20
        self.generator.parameters = {TASK:SVM, FEATURE_FILE:feature_file, SAMPLE:sample, NUMBER_OF_THREADS:number_of_threads}
        expected = dict(class_name="com.groupon.ml.svm.LibSvmTrainer",
                        parameters=dict(find_best_parameters=True,
                                        weights="0:1.0,1:6.0",
                                        sample=sample,
                                        number_of_threads=number_of_threads,
                                        representer=dict(class_name="com.groupon.nakala.core.TfFeatureWeightTextRepresenter",
                                                         parameters=dict(normalize_by_length=True,
                                                                         features=dict(class_name="com.groupon.nakala.core.Features",
                                                                                       parameters=dict(file_name=feature_file[0])),
                                                                         tokenizer=dict(class_name="com.groupon.nakala.core.RegexpTokenizerStream"),
                                                                         normalizers=[{'class_name':'com.groupon.nakala.normalization.MarkupRemover'},
                                                                                      {'class_name':'com.groupon.nakala.normalization.CaseNormalizer'},
                                                                                      {'class_name':'com.groupon.nakala.normalization.NumberNormalizer'}]))
                                       )
                       )
        self.assertEquals(expected, self.generator._get_svm_learner_config())

    def test_get_quiet_learner_config_default(self):
        feature_file=["features.tsv"]
        self.generator.parameters = {TASK:QUIET, FEATURE_FILE:feature_file}
        expected = dict(class_name="com.groupon.ml.quiet.QueryExtractorCollectionAnalyzer",
                        parameters=dict(features=dict(class_name="com.groupon.nakala.core.Features",
                                                      parameters=dict(file_name=feature_file[0])),
                                        min_tp=5,
                                        generate_negative_queries=False,
                                        batch_size=10000,
                                        min_precision=0.95))
        self.assertEquals(expected, self.generator._get_quiet_learner_config())

    def test_get_quiet_learner_config(self):
        feature_file=["features.tsv"]
        target_class = "test_class"
        number_of_threads = 10
        overwrite=True
        index_dir="/tmp/index"
        self.generator.parameters = {TASK:QUIET, FEATURE_FILE:feature_file, TARGET_CLASS:target_class, NUMBER_OF_THREADS:number_of_threads,
                                     OVERWRITE:overwrite, INDEX_DIR:index_dir, SAMPLE:0.1}
        expected = dict(class_name="com.groupon.ml.quiet.QueryExtractorCollectionAnalyzer",
                        parameters=dict(features=dict(class_name="com.groupon.nakala.core.Features",
                                                      parameters=dict(file_name=feature_file[0])),
                                        sample=0.1,
                                        min_tp=5,
                                        generate_negative_queries=False,
                                        batch_size=10000,
                                        min_precision=0.95,
                                        target_class=target_class,
                                        number_of_threads=number_of_threads,
                                        index_dir=index_dir,
                                        overwrite=overwrite))
        self.assertEquals(expected, self.generator._get_quiet_learner_config())

    def test_get_tokenizer_config(self):
        self.assertEquals(dict(class_name="com.groupon.nakala.core.RegexpTokenizerStream"), self.generator._get_tokenizer_config())
    
    def test_get_normalizers_config(self):
        expected = [dict(class_name="com.groupon.nakala.normalization.MarkupRemover"),
                       dict(class_name="com.groupon.nakala.normalization.CaseNormalizer"),
                       dict(class_name="com.groupon.nakala.normalization.NumberNormalizer")]
        self.assertEquals(expected, self.generator._get_normalizers_config())
    
    def test_get_feature_config_svm(self):
        max_feature_size = 10000
        min_feature_weight = 0.05
        target_class = 'test_class'
        self.generator.parameters = {TASK:SVM, MAX_FEATURE_SIZE:max_feature_size, MIN_FEATURE_WEIGHT:min_feature_weight, TARGET_CLASS:target_class}
        
        config_params = {TARGET_CLASS: target_class,
                         USE_ABSOLUTE_VALUES:True,
                         MAX_FEATURE_SIZE:max_feature_size,
                         MIN_FEATURE_WEIGHT:min_feature_weight,
                         'tokenizer':{'class_name':'com.groupon.nakala.core.RegexpTokenizerStream'},
                         'normalizers':[{'class_name':'com.groupon.nakala.normalization.MarkupRemover'},
                            {'class_name':'com.groupon.nakala.normalization.CaseNormalizer'},
                            {'class_name':'com.groupon.nakala.normalization.NumberNormalizer'}],
                         'min_df':3}
        
        expected = dict(class_name="com.groupon.nakala.analysis.BnsWeightCalculator", parameters=config_params)
        
        self.assertEquals(expected, self.generator._get_feature_config())
        
    def test_get_feature_config_svm_default_values(self):
        self.generator.parameters = {TASK:SVM}
        
        config_params = {USE_ABSOLUTE_VALUES:True,
                         MAX_FEATURE_SIZE:40000,
                         MIN_FEATURE_WEIGHT:0.01,
                         'tokenizer':{'class_name':'com.groupon.nakala.core.RegexpTokenizerStream'},
                         'normalizers':[{'class_name':'com.groupon.nakala.normalization.MarkupRemover'},
                            {'class_name':'com.groupon.nakala.normalization.CaseNormalizer'},
                            {'class_name':'com.groupon.nakala.normalization.NumberNormalizer'}],
                         'min_df':3}
        
        expected = dict(class_name="com.groupon.nakala.analysis.BnsWeightCalculator", parameters=config_params)
        
        self.assertEquals(expected, self.generator._get_feature_config())

    def test_get_feature_config_quiet_default_values(self):
        self.generator.parameters = {TASK:QUIET}
        
        config_params = {USE_ABSOLUTE_VALUES:False,
                         MAX_FEATURE_SIZE:40000,
                         MIN_FEATURE_WEIGHT:0.01,
                         'tokenizer':{'class_name':'com.groupon.nakala.core.RegexpTokenizerStream'},
                         'normalizers':[{'class_name':'com.groupon.nakala.normalization.MarkupRemover'},
                            {'class_name':'com.groupon.nakala.normalization.CaseNormalizer'},
                            {'class_name':'com.groupon.nakala.normalization.NumberNormalizer'}],
                         'min_df':3}
        
        expected = dict(class_name="com.groupon.nakala.analysis.BnsWeightCalculator", parameters=config_params)
        
        self.assertEquals(expected, self.generator._get_feature_config())

    def test_get_collection_reader_config(self):
        input_file = "input_file.tsv"
        id_field = 0
        label_field = 1
        text_field = 2
        
        self.generator.parameters = {INPUT_FILE:input_file, ID_FIELD:id_field, LABEL_FIELD:label_field, TEXT_FIELD:text_field}
        expected = dict(class_name="com.groupon.nakala.db.TsvCategorizedTextCollectionReader",
                        parameters=dict(file_name=input_file,
                                        separator="\\t",
                                        id_field=id_field,
                                        label_field=label_field,
                                        text_field=text_field))
        self.assertEquals(expected, self.generator._get_collection_reader_config())
        
        self.generator.parameters = {INPUT_FILE:input_file, ID_FIELD:id_field, TEXT_FIELD:text_field}
        expected = dict(class_name="com.groupon.nakala.db.TsvIdentifiableTextCollectionReader",
                        parameters=dict(file_name=input_file,
                                        separator="\\t",
                                        id_field=id_field,
                                        text_field=text_field))
        self.assertEquals(expected, self.generator._get_collection_reader_config())
    
if __name__ == '__main__':
    unittest.main()
