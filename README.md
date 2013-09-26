Nakala
======

Groupon text mining package

Introduction
============

Nakala is a text mining framework inspired by UIMA. It contains a number of core classes organized in a framework 
that allows for rapid prototyping and maximizes code reuse, all resulting in quicker deployment to production.
 
Nakala's SimpleJobFlow takes Yaml configuration files that define where the input data lives, how it should be
processed and where the results should be stored.

Framework
=========

The framework is based on seven basic interfaces:

 - Analyzable: An object that contains a text content that is to be analyzed
 - Analyzer: A class that takes an Analyzable and returns an Analysis
 - Analysis: The output of an Analyzer
 - CollectionReader: An object that reads a data source and produces Analyzables
 - CollectionAnalyzer: A class that takes a CollectionReader and returns an AnalysisCollector
 - AnalysisCollector: The output of a CollectionAnalyzer. This could be either a single analysis on the entire collection or an analysis per each item in the collection.
 - DataStore: A class that stores an Analysis

One common way to use the package is to define job flows. At this time, two types of work job are defined: SimpleJobFlow and MultiThreadedJobFlow.
A SimpleJobFlow takes a CollectionReader, a CollectionAnalyzer and a list of DataStores. It then runs the CollectionAnalyzer on the data returned by the CollectionReader, and stores the results in the DataStores. The MultiThreadedJobFlow does the same except it runs many instances of the CollectionAnalyzers in parallel.

Package Highlights
==================

1. [com.groupon.ml](/src/main/java/com/groupon/ml): A set of machine learning tools for text classification.
2. [com.groupon.nakala.analysis](/src/main/java/com/groupon/nakala/analysis): A number of analyzers and analysis containers.
3. [com.groupon.nakala.core](/src/main/java/com/groupon/nakala/core): Some core tools and classes including basic NLP tools
stemming, part-of-speech tagging, named entity recognition, and chunking.
4. [com.groupon.nakala.db](/src/main/java/com/groupon/nakala/db): Data input/output classes.
5. [com.groupon.nakala.normalization](/src/main/java/com/groupon/nakala/normalization): A set of string normalization classes.
6. [com.groupon.nakala.sentiment](/src/main/java/com/groupon/nakala/sentiment): Tools to perform sentiment analysis and review snippet extraction.
7. [com.groupon.util](/src/main/java/com/groupon/util): Useful utility classes.

Getting Started
===============

Download the package
--------------------

```
git clone https://github.com/groupon/nakala.git
```

Build it
--------

```
cd nakala
mvn package
```

Nakala contains many tools to perform NLP and text mining tasks. In this guide we will focus on training,
testing, and applying an SVM text classifier. Look in the jobflows folder for a number of different 
configuration examples.

Train a text classifier
-----------------------

Training a text classifier is performed in two steps: feature selection, and training. You can also test
your trained classifier on some unseen test data.

Let's assume you have a tab-separated training corpus at /home/jdoe/train.tsv with the following format:

```
record-id    categories    text
```

where "categories" is a comma-separated list of category labels.

### Feature selection

To perform feature selection, create a YAML file like the one below as follows:

```
python script/generate_yaml_file.py --task svm --phase feature \
    --input-file /home/jdoe/train.tsv \
    --id-field 0 --label-field 1 --text-field 2 \
    --data-store /home/jdoe/features.tsv \
    --output /home/jdoe/feature_select.yml \
    --max-feature-size 30000 --min-feature-weight 0.05
```

The above command will generate the file /home/jdoe/feature_select.yml with contents similar
to the following:

```
collection_reader:
  class_name: com.groupon.nakala.db.TsvCategorizedTextCollectionReader
  parameters:
    file_name: /home/jdoe/train.tsv
    separator: \t
    id_field: 0        # 0-based index of the record id
    label_field: 1     # 0-based index of the comma-separated labels
    text_field: 2      # 0-based index of the website text

collection_analyzer:
  class_name: com.groupon.nakala.analysis.BnsWeightCalculator
  parameters:
#    target_class: TARGET_CLASS_NAME # only needed if you want to train a binary classifier for a particular class
    use_absolute_values: true
    max_feature_size: 30000     # hard cap on the size of feature set
    min_feature_weight: 0.05    # minimum feature score to be included in the feature set
    tokenizer:
      class_name: com.groupon.nakala.core.RegexpTokenizerStream
    normalizers:
      - class_name: com.groupon.nakala.normalization.MarkupRemover
      - class_name: com.groupon.nakala.normalization.CaseNormalizer
      - class_name: com.groupon.nakala.normalization.NumberNormalizer
    min_df: 3    # minimum document frequency of a feature to be included in the feature set
#    stopwords:  # not required. the stopwords list specified below contain 500 English stopwords. Should not be used for other languages
#      file_name: <PATH_TO_STOP_LIST_FILE>

data_stores:
  - class_name: com.groupon.nakala.db.FlatFileStore
    parameters:
      file_name: /home/jdoe/features.tsv
```

Now run this task:

```
script/simpleJobFlow -c /home/jdoe/feature_select.yml
```

This will generate the file /home/jdoe/features.tsv.

### Training

For training we need a new YAML file.

```
python generate_yaml_file.py --task svm --phase learn --input-file /home/jdoe/train.tsv \
    --id-field 0 --label-field 1 --text-field 2 \
    --feature-file /home/jdoe/features.tsv \
    --data-store /home/jdoe/model \
    --output /home/jdoe/train.yml \
    --number-of-threads 4
```

This will generate a YAML file named /home/jdoe/train.yml with content similar to the following:

```
collection_reader:
  class_name: com.groupon.nakala.db.TsvCategorizedTextCollectionReader
    parameters:
      file_name: /home/jdoe/train.tsv
      separator: \t
      id_field: 0        # 0-based index of the record id
      label_field: 1     # 0-based index of the comma-separated labels
      text_field: 2      # 0-based index of the website text
  
collection_analyzer:
  class_name: com.groupon.ml.svm.LibSvmTrainerner
  parameters:
#    target_class: TARGET_CLASS_NAME       # Needed if building a binary classifier.
    find_best_parameters: true    # set to true to perform grid search
    number_of_threads: 4          # number of threads to use to parallelize grid search
    sample: 0.2    # 20% sample size for parameter optimization. Use this for large training sets. If unspecified, all training data is used in grid search.
    # weights: 0:1.0,1:6.0  # This is to give the second label in alphabetical order more weight.
    # c: 1.0 # default: 1 -- can be used when find_best_parameters is false
    # gamma: 0.0001 # default: 1/number of features -- can be used when find_best_parameter is false
    representer:
      class_name: com.groupon.nakala.core.TfFeatureWeightTextRepresenter
      parameters:
        normalize_by_length: true
        features:
          class_name: com.groupon.nakala.core.Features
          parameters:
            file_name: /home/jdoe/features.tsv
        tokenizer:
          class_name: com.groupon.nakala.core.RegexpTokenizerStream
        normalizers:
          - class_name: com.groupon.nakala.normalization.MarkupRemover
          - class_name: com.groupon.nakala.normalization.CaseNormalizer
          - class_name: com.groupon.nakala.normalization.NumberNormalizer

data_stores:
  - class_name: com.groupon.nakala.db.FlatFileStore
    parameters:
      # file_name here is actually a file stem: This collection analyzer produces three files:
      #    filestem.model  : the svm model
      #    filestem.range  : value range file for scaling new data
      #    filestem.labels : list of labels in the training data for labeling new data
      file_name: /home/jdoe/model
```

Now run this training task:

```
script/simpleJobFlow -c /home/jdoe/train.yml
```

The training task will generate four files:

- SVM model file: /home/jdoe/model.mod
- Value ranges used for normalization: /home/jdoe/model.range
- Labels used in the model: /home/jdoe/model.labels
- Best parameters found (for information only): /home/jdoe/model.params

Test the classifier
-------------------

Let's assume you have the test file /home/jdoe/test.tsv with the same format as the training file.
To test the classifier using this data generate another YAML configuration file as follows:

```
python generate_yaml_file.py --task svm --phase evaluate --input-file /home/jdoe/test.tsv \
    --id-field 0 --label-field 1 --text-field 2 \
    --feature-file /home/jdoe/features.tsv \
    --svm-output-stem /home/jdoe/model \
    --data-store /home/jdoe/report.xls
```

This will generate a YAML file similar to this:

```
collection_reader:
  class_name: com.groupon.nakala.db.TsvCategorizedTextCollectionReader
    parameters:
      file_name: /home/jdoe/test.tsv
      separator: \t
      id_field: 0        # 0-based index of the record id
      label_field: 1     # 0-based index of the comma-separated labels
      text_field: 2      # 0-based index of the website text
    
collection_analyzer:
  class_name: com.groupon.ml.ClassifierEvaluator
  parameters:
    # target_class: TARGET_CLASS_NAME # only needed when targeting a specific class
    min_threshold: 0.0  # min score threshold to report on
    max_threshold: 1.0  # max score threshold to report on
    threshold_step: 0.1 # threshold steps
    analyzer:
      class_name: com.groupon.ml.svm.LibSvmTextClassifier
      parameters:
        model:
          class_name: com.groupon.nakala.core.ResourceReader
          parameters:
            file_name: /home/jdoe/model.model
        labels:
          class_name: com.groupon.nakala.core.ResourceReader
          parameters:
            file_name: /home/jdoe/model.labels
        representer:
          class_name: com.groupon.nakala.core.TfFeatureWeightTextRepresenter
          parameters:
            normalize_by_length: true
            features:
              class_name: com.groupon.nakala.core.Features
              parameters:
                file_name: /home/jdoe/features.tsv
            tokenizer:
              class_name: com.groupon.nakala.core.RegexpTokenizerStream
            normalizers:
              - class_name: com.groupon.nakala.normalization.MarkupRemover
              - class_name: com.groupon.nakala.normalization.CaseNormalizer
              - class_name: com.groupon.nakala.normalization.NumberNormalizer
            scaler:
              class_name: com.groupon.ml.svm.ValueScaler
              parameters:
                file_name: /home/jdoe/model.range

data_stores:
  - class_name: com.groupon.nakala.db.FlatFileStore
    parameters:
      file_name: /home/jdoe/report.xls
```

Now run this test task:

```
script/simpleJobFlow -c /home/jdoe/test.yml
```

The task will run the trained classifier on the test data and generate an Excel report file.
The report contains precision, recall, and F-score numbers for the range of score thresholds specified in
the configuration file, as well as the detailed output from the classifier.

Run classifier on new data
--------------------------

Once you have one or more binary or n-ary model(s), you will want to use those models to classify
new text documents. Let's assume your unclassified data is in /home/jdoe/new_data.tsv, and it contains
two columns one id and the other the text to be classified. To classify these data, generate another
YAML file:

```
python generate_yaml_file.py --task svm --phase classify --input-file /home/jdoe/new_data.tsv \
    --id-field 0 --text-field 1 \
    --feature-file /home/jdoe/features.tsv
    --svm-output-stem /home/jdoe/model \
    --data-store /home/jdoe/new_classifications.tsv
    --output /home/jdoe/classify.yml \
```

This will generate the file /home/jdoe/classify.yml with content similar to the following:

```
collection_reader:
  class_name: com.groupon.textmining.db.TsvIdentifiableTextCollectionReader
  parameters:
    file_name: /home/jdoe/new_data.tsv 
    separator: \t
    id_field: 0      # 0-based index of record id
    text_field: 1    # 0-based index of website text

collection_analyzer:
  class_name: com.groupon.ml.ClassifierCollectionAnalyzer
  parameters:
    analyzer:
      class_name: com.groupon.ml.svm.MultiModelClassifier
      parameters:
        analyzers:
              - class_name: com.groupon.ml.svm.LibSvmTextClassifier
                parameters:
                  model:
                    class_name: com.groupon.textmining.core.ResourceReader
                    parameters:
                      file_name: /home/jdoe/model.model
                  labels:
                    class_name: com.groupon.textmining.core.ResourceReader
                    parameters:
                      file_name: /home/jdoe/model.labels
                  representer:
                    class_name: com.groupon.textmining.core.TfFeatureWeightTextRepresenter
                    parameters:
                      normalize_by_length: true
                      features:
                        class_name: com.groupon.textmining.core.Features
                        parameters:
                          file_name: /home/jdoe/features.tsv
                      tokenizer:
                        class_name: com.groupon.textmining.core.RegexpTokenizerStream
                      normalizers:
                        - class_name: com.groupon.textmining.normalization.MarkupRemover
                        - class_name: com.groupon.textmining.normalization.CaseNormalizer
                        - class_name: com.groupon.textmining.normalization.NumberNormalizer
                      scaler:
                        class_name: com.groupon.ml.svm.ValueScaler
                        parameters:
                          file_name: /home/jdoe/model.range

data_stores:
  - class_name: com.groupon.textmining.db.FlatFileStore
    parameters:
      file_name: /home/jdoe/new_classifications.tsv
```

And finally run the classifier:

```
script/simpleJobFlow -c /home/jdoe/classify.yml
```

The output of the classifier will be saved in /home/jdoe/new_classifications.tsv. It will have the
following format:

```
id    label1:score    label2:score ...
```

License
=======

Copyright (c) 2013, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
