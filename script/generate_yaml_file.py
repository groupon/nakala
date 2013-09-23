#!/usr/bin/env python


from argparse import ArgumentParser
import os
import sys
import traceback
import yaml

### CONSTANTS #######################################

# Tasks
SVM = 'svm'          # Name
QUIET = 'quiet'    # Website classification

# Phases
FEATURE = 'feature'    # Feature extraction
LEARN = 'learn'        # Model learning
EVALUATE = 'evaluate'  # Evaluation on test set
CLASSIFY = 'classify'  # Batch classification

TASKS = [SVM, QUIET]
PHASES = [FEATURE, LEARN, EVALUATE, CLASSIFY]
TASK = "task"
PHASE = "phase"
OUTPUT = "output"

# Collection reader
INPUT_FILE = "input_file"
ID_FIELD = "id_field"
LABEL_FIELD = "label_field"
TEXT_FIELD = "text_field"

# Collection analyzer
NUMBER_OF_THREADS = "number_of_threads"
TARGET_CLASS = "target_class"
MAX_FEATURE_SIZE = "max_feature_size"
MIN_FEATURE_WEIGHT = "min_feature_weight"
SAMPLE = "sample"
FEATURE_FILE = "feature_file"
SVM_OUTPUT_STEM = "svm_output_stem"
QUIET_MODEL = "quiet_model"
USE_ABSOLUTE_VALUES = "use_absolute_values"
INDEX_DIR = "index_dir"
OVERWRITE = "overwrite"

# Data store
DATA_STORE = "data_store"

# YAML KEYS
COLLECTION_READER = "collection_reader"
COLLECTION_ANALYZER = "collection_analyzer"
DATA_STORES = "data_stores"
PARAMETERS = "parameters"
CLASS_NAME = "class_name"
SCALER = "scaler"

####################################################

class YamlFileGenerator:

    def _validate(self):
        errors = []
        used_keys = set()
        
        task = self.parameters.get(TASK)
        if task not in TASKS:
            errors.append("Task unknown or not specified.")
        else:
            used_keys.add(TASK)
        
        phase = self.parameters.get(PHASE)
        if phase not in PHASES:
            errors.append("Phase unknown or not specified.")
        else:
            used_keys.add(PHASE)

        if task == QUIET:
            if phase == FEATURE:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, LABEL_FIELD, DATA_STORE], errors, used_keys)
                self._validate_optional_params([TARGET_CLASS, MAX_FEATURE_SIZE, MIN_FEATURE_WEIGHT], used_keys)
            elif phase == LEARN:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, LABEL_FIELD, FEATURE_FILE, DATA_STORE, TARGET_CLASS], errors, used_keys)
                self._validate_optional_params([NUMBER_OF_THREADS, INDEX_DIR, SAMPLE], used_keys)
            elif phase == EVALUATE:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, LABEL_FIELD, QUIET_MODEL, DATA_STORE], errors, used_keys)
                self._validate_optional_params([TARGET_CLASS], used_keys)
                self._validate_length_equals(QUIET_MODEL, 1, errors)
            elif phase == CLASSIFY:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, QUIET_MODEL, DATA_STORE], errors, used_keys)
                self._validate_optional_params([NUMBER_OF_THREADS], used_keys)
        elif task == SVM:
            if phase == FEATURE:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, LABEL_FIELD, DATA_STORE], errors, used_keys)
                self._validate_optional_params([TARGET_CLASS, MAX_FEATURE_SIZE, MIN_FEATURE_WEIGHT], used_keys)
            elif phase == LEARN:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, LABEL_FIELD, FEATURE_FILE, DATA_STORE], errors, used_keys)
                self._validate_optional_params([TARGET_CLASS, NUMBER_OF_THREADS, SAMPLE], used_keys)
            elif phase == EVALUATE:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, LABEL_FIELD, FEATURE_FILE, SVM_OUTPUT_STEM, DATA_STORE], errors, used_keys)
                self._validate_optional_params([TARGET_CLASS], used_keys)
                self._validate_length_equals(FEATURE_FILE, 1, errors)
                self._validate_length_equals(SVM_OUTPUT_STEM, 1, errors)
            elif phase == CLASSIFY:
                self._validate_presence_of([INPUT_FILE, ID_FIELD, TEXT_FIELD, FEATURE_FILE, SVM_OUTPUT_STEM, DATA_STORE], errors, used_keys)
                self._validate_optional_params([NUMBER_OF_THREADS], used_keys)
                self._validate_list_lengths_equal(FEATURE_FILE, SVM_OUTPUT_STEM, errors)

        ret = True
        if len(used_keys) != len(self.parameters):
            errors.append("Don't know what to do with option(s) [%s]" %",".join(sorted(list(set(self.parameters.keys()) - used_keys))))
            ret = False
        
        if len(errors) > 0:
            self.parameters['error'] = '\n'.join(errors)
            ret = False
        
        return ret

    def _validate_length_equals(self, key, expected_length, errors):
        theList = self.parameters.get(key)
        if theList is None or len(theList) != expected_length:
            errors.append("Length of %s must be %d." %(key, expected_length))
            return False
        return True

    def _validate_list_lengths_equal(self, key1, key2, errors):
        list1 = self.parameters.get(key1)
        list2 = self.parameters.get(key2)
        if list1 is None or list2 is None or len(list1) != len(list2):
            errors.append("Lengths of %s and %s are not equal." %(key1, key2))
            return False
        return True
    
    def _validate_presence_of(self, keys, errors, used_keys):
        ret = True
        for key in keys:
            if self.parameters.has_key(key):
                used_keys.add(key)
            else:
                errors.append("%s not specified." %key)
                ret = False
        return ret
    
    def _validate_optional_params(self, keys, used_keys):
        for key in keys:
            if self.parameters.has_key(key):
                used_keys.add(key)

    def getConfiguration(self, parameters):
        self.parameters = parameters
        config = dict()
        
        if self._validate():
            phase = self.parameters[PHASE]
            
            config[COLLECTION_READER] = self._get_collection_reader_config()
            config[DATA_STORES] = self._get_data_stores_config()

            if phase == FEATURE:
                config[COLLECTION_ANALYZER] = self._get_feature_config()
            elif phase == LEARN:
                config[COLLECTION_ANALYZER] = self._get_learner_config()
            elif phase == EVALUATE:
                config[COLLECTION_ANALYZER] = self._get_evaluate_config()
            elif phase == CLASSIFY:
                config[COLLECTION_ANALYZER] = self._get_batch_classifier_config()
        else:
            print >>sys.stderr, self.parameters['error']
            raise Exception("Validation failed.")

        return config
    
    def _get_learner_config(self):
        task = self.parameters[TASK]
        if task == QUIET:
            return self._get_quiet_learner_config()
        elif task == SVM:
            return self._get_svm_learner_config()
    
    def _get_evaluate_config(self):
        task = self.parameters[TASK]
        if task == QUIET:
            return self._get_quiet_evaluation_config()
        elif task == SVM:
            return self._get_svm_evaluation_config()

    def _get_batch_classifier_config(self):
        task = self.parameters[TASK]
        if task == QUIET:
            return self._get_batch_classifier_config_quiet()
        elif task == SVM:
            return self._get_batch_classifier_config_svm()

    def _get_batch_classifier_config_svm(self):
        analyzers_config = self._get_svm_analyzers_config()
        analyzer_config = dict(class_name="com.groupon.ml.svm.MultiModelClassifier", parameters=dict(analyzers=analyzers_config))
        if self.parameters.has_key(NUMBER_OF_THREADS):
            analyzer_config[PARAMETERS][NUMBER_OF_THREADS] = self.parameters[NUMBER_OF_THREADS]
        return dict(class_name="com.groupon.ml.ClassifierCollectionAnalyzer", parameters=dict(analyzer=analyzer_config))
    
    def _get_batch_classifier_config_quiet(self):
        config_params = dict(models=self._get_quiet_models_config())
        if self.parameters.has_key(NUMBER_OF_THREADS):
            config_params[NUMBER_OF_THREADS] = self.parameters[NUMBER_OF_THREADS]
        
        return dict(class_name="com.groupon.ml.quiet.QuietCollectionAnalyzer",
                      parameters=config_params)

    def _get_quiet_models_config(self):
        return [self._get_quiet_model_config(quiet_model) for quiet_model in self.parameters[QUIET_MODEL]]

    def _get_quiet_model_config(self, quiet_model):
        return dict(class_name="com.groupon.ml.quiet.HummingBirdModelLoader",
                    parameters=dict(file_name=quiet_model))

    def _get_svm_analyzers_config(self):
        feature_files = self.parameters[FEATURE_FILE]
        svm_output_stems = self.parameters[SVM_OUTPUT_STEM]
        return [self._get_svm_analyzer_config(feature_file, svm_output_stem) for
                feature_file, svm_output_stem in zip(feature_files, svm_output_stems)]
    
    def _get_quiet_evaluation_config(self):
        model_config = dict(class_name="com.groupon.ml.quiet.HummingBirdModelLoader",
                            parameters=dict(file_name=self.parameters[QUIET_MODEL][0]))
        
        analyzer_params = dict(model=model_config)
        analyzer_config = dict(class_name="com.groupon.ml.quiet.HummingBirdAnalyzer", parameters=analyzer_params)
        config_params = dict(min_threshold=0.005, max_threshold=1.0, threshold_step=0.005, analyzer=analyzer_config)
        
        return dict(class_name="com.groupon.ml.ClassifierEvaluator", parameters=config_params)

    def _get_svm_analyzer_config(self, feature_file, svm_output_stem):
        analyzer_params = dict(model=dict(class_name="com.groupon.nakala.core.ResourceReader",
                                  parameters=dict(file_name=svm_output_stem + ".model")),
                       labels=dict(class_name="com.groupon.nakala.core.ResourceReader",
                                   parameters=dict(file_name=svm_output_stem + ".labels")),
                       representer=self._get_representer_config(feature_file, svm_output_stem + ".range"))
        return dict(class_name="com.groupon.ml.svm.LibSvmTextClassifier", parameters=analyzer_params)

    def _get_svm_evaluation_config(self):
        analyzer_config = self._get_svm_analyzer_config(self.parameters[FEATURE_FILE][0], self.parameters[SVM_OUTPUT_STEM][0])
        config_params = dict(min_threshold=0.05, max_threshold=1.0, threshold_step=0.05, analyzer=analyzer_config)
        if self.parameters.has_key(TARGET_CLASS):
            config_params[TARGET_CLASS] = self.parameters[TARGET_CLASS]
        return dict(class_name="com.groupon.ml.ClassifierEvaluator", parameters=config_params)
    
    def _get_data_stores_config(self):
        class_name = "com.groupon.nakala.db.FlatFileStore"
        if self.parameters[TASK] == QUIET and self.parameters[PHASE] == LEARN:
            class_name = "com.groupon.nakala.db.SerializationStore"
        return [dict(class_name=class_name, parameters=dict(file_name=self.parameters[DATA_STORE]))]
    
    def _get_representer_config(self, feature_file, range_file=None):
        config = dict(class_name="com.groupon.nakala.core.TfFeatureWeightTextRepresenter",
                    parameters=dict(normalize_by_length=True,
                    features=dict(class_name="com.groupon.nakala.core.Features",
                                  parameters=dict(file_name=feature_file)),
                    tokenizer=self._get_tokenizer_config(),
                    normalizers=self._get_normalizers_config()))
        
        if range_file:
            config['parameters'][SCALER] = dict(class_name="com.groupon.ml.svm.ValueScaler",
                                                parameters=dict(file_name=range_file))
        return config
    
    def _get_svm_learner_config(self):
        representer = self._get_representer_config(self.parameters[FEATURE_FILE][0])
        config_params = dict(find_best_parameters=True, representer=representer)
        
        if self.parameters.has_key(TARGET_CLASS):
            config_params[TARGET_CLASS] = self.parameters[TARGET_CLASS]
        
        if self.parameters.has_key(NUMBER_OF_THREADS):
            config_params[NUMBER_OF_THREADS] = self.parameters[NUMBER_OF_THREADS]
        
        if self.parameters.has_key(SAMPLE):
            config_params[SAMPLE] = self.parameters[SAMPLE]
        
        return dict(class_name="com.groupon.ml.svm.LibSvmTrainer", parameters=config_params)
    
    def _get_quiet_learner_config(self):
        features_params = dict(class_name="com.groupon.nakala.core.Features",
                               parameters=dict(file_name=self.parameters[FEATURE_FILE][0]))
        config_params = dict(features=features_params, generate_negative_queries=False, batch_size=10000, min_precision=0.95, min_tp=5)
        
        if self.parameters.has_key(NUMBER_OF_THREADS):
            config_params[NUMBER_OF_THREADS] = self.parameters[NUMBER_OF_THREADS]
        
        if self.parameters.has_key(TARGET_CLASS):
            config_params[TARGET_CLASS] = self.parameters[TARGET_CLASS]
        
        if self.parameters.has_key(INDEX_DIR):
            config_params[INDEX_DIR] = self.parameters[INDEX_DIR]
            config_params[OVERWRITE] = True
        
        if self.parameters.has_key(SAMPLE):
            config_params[SAMPLE] = self.parameters[SAMPLE]

        return dict(class_name="com.groupon.ml.quiet.QueryExtractorCollectionAnalyzer", parameters=config_params)
    
    def _get_tokenizer_config(self):
        return dict(class_name="com.groupon.nakala.core.RegexpTokenizerStream")
    
    def _get_normalizers_config(self):
        return [dict(class_name="com.groupon.nakala.normalization.MarkupRemover"),
                       dict(class_name="com.groupon.nakala.normalization.CaseNormalizer"),
                       dict(class_name="com.groupon.nakala.normalization.NumberNormalizer")]
    
    def _get_feature_config(self):
        config_params = dict(tokenizer=self._get_tokenizer_config(), normalizers=self._get_normalizers_config(), min_df=3)
        
        if self.parameters.has_key(TARGET_CLASS):
            config_params[TARGET_CLASS] = self.parameters[TARGET_CLASS]
        
        # QuIET (used in name classification and service identification) needs absolute values of BNS scores as feature weights.
        # LibSVMTrainer (used in website classification) needs signed BNS scores as feature weights.
        if self.parameters[TASK] == QUIET:
            config_params[USE_ABSOLUTE_VALUES] = False
        elif self.parameters[TASK] == SVM:
            config_params[USE_ABSOLUTE_VALUES] = True
        
        config_params[MAX_FEATURE_SIZE] = self.parameters.get(MAX_FEATURE_SIZE)
        if not config_params[MAX_FEATURE_SIZE]:
            config_params[MAX_FEATURE_SIZE] = 40000
        
        config_params[MIN_FEATURE_WEIGHT] = self.parameters.get(MIN_FEATURE_WEIGHT)
        if not config_params[MIN_FEATURE_WEIGHT]:
            config_params[MIN_FEATURE_WEIGHT] = 0.01
        
        return dict(class_name="com.groupon.nakala.analysis.BnsWeightCalculator",
                      parameters=config_params)
    
    def _get_collection_reader_config(self):
        class_name = "com.groupon.nakala.db.TsvIdentifiableTextCollectionReader"
        cr_parameters = dict(file_name=self.parameters[INPUT_FILE],                             
                            separator="\\t",
                            id_field=self.parameters[ID_FIELD],
                            text_field=self.parameters[TEXT_FIELD])
        if self.parameters.has_key(LABEL_FIELD):
            cr_parameters[LABEL_FIELD] = self.parameters[LABEL_FIELD]
            class_name="com.groupon.nakala.db.TsvCategorizedTextCollectionReader"
        return dict(class_name=class_name,
                    parameters=cr_parameters)

    def saveConfiguration(self, parameters):
        output = parameters.get(OUTPUT)
        
        if not output or output == "-":
            print yaml.dump(self.getConfiguration(parameters), default_flow_style=False)
        else:
            dir_path = os.path.dirname(output)
            if dir_path and not os.path.isdir(dir_path):
                os.makedirs(dir_path)
            del parameters[OUTPUT]
            yaml.dump(self.getConfiguration(parameters), open(output, 'w'), default_flow_style=False)

def main():
    parser = ArgumentParser(description="Generates a YAML file for a particular phase of a nakala classification task. For more information visit https://wiki.groupondev.com/Merchant_Data/Categorization_and_Service_Identification")
    # General parameters
    parser.add_argument("-t", "--task",   dest=TASK,   type=str, required=True, choices = TASKS, help="Task type; 'name' for name classification, 'website' for website classification, 'service' for service identification")
    parser.add_argument("-p", "--phase",  dest=PHASE,  type=str, required=True, choices = PHASES, help="Task phase; 'feature' for feature extraction, 'learn' for learning, 'evaluate' for evaluation on test data, 'classify' for batch classification of new data")
    parser.add_argument("-o", "--output", dest=OUTPUT, type=str, help="Output YAML file name. Omit or use '-' for stdout.")
    
    # Collection reader parameters
    parser.add_argument("--input-file",   dest=INPUT_FILE,  required=True, type=str, help="Path to training/test file.")
    parser.add_argument("--id-field",     dest=ID_FIELD,    required=True, type=int, help="0-based index of record id in file in training/test data.")
    parser.add_argument("--label-field",  dest=LABEL_FIELD,                type=int, help="0-based index of comma-separated true labels in training/test data.")
    parser.add_argument("--text-field",   dest=TEXT_FIELD,  required=True, type=int, help="0-based index of input text in training/test data.")
    
    # Collection analyzer parameters
    parser.add_argument("--number-of-threads",  dest=NUMBER_OF_THREADS,  type=int,   help="Number of threads used by collection analyzer or QuIET.")
    #   Feature selector parameters
    parser.add_argument("--target-class",       dest=TARGET_CLASS,       type=str,   help="Target class to train/test for in case of multiclass corpus. Not needed if corpus is already binary.")
    parser.add_argument("--max-feature-size",   dest=MAX_FEATURE_SIZE,   type=int,   help="Maximum feature size for feature selection.")
    parser.add_argument("--min-feature-weight", dest=MIN_FEATURE_WEIGHT, type=float, help="Minimum feature weight to include in feature set.")
    #   QuIET specific parameters
    parser.add_argument("--index-dir",          dest=INDEX_DIR,          type=str,   help="Path to lucene index directory. If unspecified, RAMDirectory will be used.")
    #   LibSVMLearner specific parameters
    parser.add_argument("--sample",             dest=SAMPLE,             type=float, help="Proportion of training data to sample in parameter optimization (e.g., 0.2 for a 20%% sample).")
    parser.add_argument("--feature-file",       dest=FEATURE_FILE,       type=str,   nargs="+", help="Path TSV feature file(s). In case of multimodel classification used in batch classification, more than one feature file can be specified. In case of website classification, the number of feature files and svm output stems must be equal.")
    #   Evaluation parameters
    parser.add_argument("--svm-output-stem",    dest=SVM_OUTPUT_STEM,    type=str,   nargs="+", help="Path to the output of LibSVMLearner file stem(s). In case of multimodel classification used in batch classification, more than one svm output stem may be specified.")
    parser.add_argument("--quiet-model",        dest=QUIET_MODEL,        type=str,   nargs="+", help="Path to QuIET model. In case of multimodel batch classification, more than one model may be specified.")
    # Data store parameters
    parser.add_argument("--data-store",         dest=DATA_STORE,         required=True, type=str,   help="Path to output of SimpleJobFlow.")
    
    options = dict([(k,v) for (k,v) in vars(parser.parse_args()).items() if v is not None])
    
    generator = YamlFileGenerator()
    try:
        generator.saveConfiguration(options)
    except:
        print >>sys.stderr, "Error: %s" %sys.exc_info()[1]
        traceback.print_exc(file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    main()
