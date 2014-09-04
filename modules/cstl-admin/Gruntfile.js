module.exports = function(grunt) {
    'use strict';

    grunt.initConfig({

        lib_dir: './grunt/lib',
        src_dir: './grunt/src',
        target_dir: grunt.option('target_dir') || './target/cstl-admin',

        // Clean build directory.
        clean: ['<%= target_dir %>'],

        // Validate JavaScript code style.
        jshint: {
            app: {
                options: {
                    'curly': true,
                    'eqnull': true,
                    'eqeqeq': true,
                    'nonew':true,
                    'noarg':true,
                    'forin':true,
                    'noempty':true,
                    'undef':true,
                    'bitwise':true,
                    'browser':true,
                    'jquery':true,
                    'latedef':true,
                    'immed':true,
                    'freeze':true,
                    'globals':{
                        "angular":true,
                        "Growl":true,
                        "$translate":true,
                        "console":true,
                        "confirm":true,
                        "OpenLayers":true,
                        "d3":true,
                        "alert":true,
                        "Stomp":true,
                        "SockJS":true,
                        "c3":true,
                        "dataNotReady":true,
                        "cstlAdminApp":true,
                        "DataViewer":true,
                        "WmtsViewer":true,
                        "Netcdf":true
                    }
                },
                src: ['<%= src_dir %>/js/**/*.js']
            }
        },

        // Copy assets files.
        copy: {
            app: {
                files: [
                    {
                        src: ['*.html'],
                        cwd: '<%= src_dir %>/',
                        dest: '<%= target_dir %>/',
                        expand: true
                    },
                    {
                        src: ['img/**'],
                        cwd: '<%= src_dir %>/',
                        dest: '<%= target_dir %>/',
                        expand: true
                    },
                    {
                        src: ['views/**'],
                        cwd: '<%= src_dir %>/',
                        dest: '<%= target_dir %>/',
                        expand: true
                    },
                    {
                        src: ['i18n/*.json'],
                        cwd: '<%= src_dir %>/',
                        dest: '<%= target_dir %>/',
                        expand: true
                    },
                    {
                        src: ['metadata/*.json'],
                        cwd: '<%= src_dir %>/',
                        dest: '<%= target_dir %>/',
                        expand: true
                    }
                ]
            },
            lib: {
                files: [
                    {
                        src: ['<%= lib_dir %>/**/css/*'],
                        dest: '<%= target_dir %>/css',
                        expand: true,
                        flatten: true
                    },
                    {
                        src: ['<%= lib_dir %>/**/fonts/*'],
                        dest: '<%= target_dir %>/fonts',
                        expand: true,
                        flatten: true
                    },
                    {
                        src: ['<%= lib_dir %>/**/img/*'],
                        dest: '<%= target_dir %>/img',
                        expand: true,
                        flatten: true
                    },
                    {
                        src: ['<%= lib_dir %>/**/images/*'],
                        dest: '<%= target_dir %>/images',
                        expand: true,
                        flatten: true
                    },
                    {
                        src: ['<%= lib_dir %>/**/views/*'],
                        dest: '<%= target_dir %>/views',
                        expand: true,
                        flatten: true
                    }
                ]
            }
        },

        // Compile less files.
        less: {
            app: {
                options: {
                    compress: true,
                    cleancss: true
                },
                files: {
                    '<%= target_dir %>/css/app.css': '<%= src_dir %>/less/app.less'
                }
            },
            lib: {
                options: {
                    compress: true,
                    cleancss: true
                },
                files: {
                    '<%= target_dir %>/css/angular.min.css': '<%= lib_dir %>/angular/less/angular.less',
                    '<%= target_dir %>/css/bootstrap.min.css': '<%= lib_dir %>/bootstrap/less/bootstrap.less',
                    '<%= target_dir %>/css/c3.min.css': '<%= lib_dir %>/c3/less/c3.less',
                    '<%= target_dir %>/css/famfamfam-flags.min.css': '<%= lib_dir %>/famfamfam-flags/less/famfamfam-flags.less',
                    '<%= target_dir %>/css/font-awesome.min.css': '<%= lib_dir %>/font-awesome/less/font-awesome.less',
                    '<%= target_dir %>/css/highlight.min.css': '<%= lib_dir %>/highlight/less/highlight.less',
                    '<%= target_dir %>/css/jquery.min.css': '<%= lib_dir %>/jquery/less/jquery.less',
                    '<%= target_dir %>/css/openlayers.min.css': '<%= lib_dir %>/openlayers/less/openlayers.less'
                }
            }
        },

        // Merge script files.
        concat: {
            app: {
                options: {
                    banner: '(function(window, angular, undefined) {\'use strict\';',
                    footer: '})(window, window.angular);'
                },
                files: {
                    '<%= target_dir %>/js/app.js': [
                        '<%= src_dir %>/js/app.js',
                        '<%= src_dir %>/js/directives.js',
                        '<%= src_dir %>/js/restapi.js',
                        '<%= src_dir %>/js/services.js',
                        '<%= src_dir %>/js/controllers.js',
                        '<%= src_dir %>/js/admin/*.js',
                        '<%= src_dir %>/js/mapcontext/*.js',
                        '<%= src_dir %>/js/process/*.js',
                        '<%= src_dir %>/js/sensor/*.js',
                        '<%= src_dir %>/js/webservice/*.js',
                        '<%= src_dir %>/js/controller-data.js',
                        '<%= src_dir %>/js/controller-data-import.js',
                        '<%= src_dir %>/js/controller-style.js',
                        '<%= src_dir %>/js/cstl.data.viewer.js',
                        '<%= src_dir %>/js/cstl.viewer.wmts.js',
                        '<%= src_dir %>/js/cstl.netcdf.js'
                    ]
                }
            },
            app_index: {
                options: {
                    banner: '(function(window, angular, undefined) {\'use strict\';',
                    footer: '})(window, window.angular);'
                },
                files: {
                    '<%= target_dir %>/js/app-index.js': [
                        '<%= src_dir %>/js/app-index.js',
                        '<%= src_dir %>/js/directives.js',
                        '<%= src_dir %>/js/restapi.js',
                        '<%= src_dir %>/js/services.js'
                    ]
                }
            },
            lib: {
                files: {
                    '<%= target_dir %>/js/ace.min.js': '<%= lib_dir %>/ace/js/*.js',
                    '<%= target_dir %>/js/angular.min.js': '<%= lib_dir %>/angular/js/*.js',
                    '<%= target_dir %>/js/bootstrap.min.js': '<%= lib_dir %>/bootstrap/js/*.js',
                    '<%= target_dir %>/js/c3.min.js': '<%= lib_dir %>/c3/js/*.js',
                    '<%= target_dir %>/js/d3.min.js': '<%= lib_dir %>/d3/js/*.js',
                    '<%= target_dir %>/js/highlight.min.js': '<%= lib_dir %>/highlight/js/*.js',
                    '<%= target_dir %>/js/jquery.min.js': '<%= lib_dir %>/jquery/js/*.js',
                    '<%= target_dir %>/js/openlayers.min.js': '<%= lib_dir %>/openlayers/js/*.js',
                    '<%= target_dir %>/js/sockjs.min.js': '<%= lib_dir %>/sockjs/js/*.js',
                    '<%= target_dir %>/js/stomp.min.js': '<%= lib_dir %>/stomp/js/*.js'
                }
            }
        },

        // Annotate AngularJS application script files for obfuscation.
        ngAnnotate: {
            app: {
                src: ['<%= target_dir %>/js/app.js'],
                dest: '<%= target_dir %>/js/app.js'
            },
            app_index: {
                src: ['<%= target_dir %>/js/app-index.js'],
                dest: '<%= target_dir %>/js/app-index.js'
            }
        },

        // Obfuscate application script files.
        uglify: {
            app: {
                src: ['<%= target_dir %>/js/app.js'],
                dest: '<%= target_dir %>/js/app.js'
            },
            app_index: {
                src: ['<%= target_dir %>/js/app-index.js'],
                dest: '<%= target_dir %>/js/app-index.js'
            }
        }

// TODO → Minify template files for better performances.
// Previous attempts using following configuration with 'grunt-contrib-htmlmin' have broken some page layouts.
//
//        // Minify application templates files.
//        htmlmin: {
//            app: {
//                options: {
//                    removeComments: true,
//                    collapseWhitespace: true
//                },
//                files: [{
//                    cwd: '<%= target_dir %>/views/',
//                    src: '**/*.html',
//                    dest: '<%= target_dir %>/views/',
//                    expand: true
//                }]
//            }
//        }
    });

    // Load NPM tasks.
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
//    grunt.loadNpmTasks('grunt-contrib-htmlmin');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-ng-annotate');

    // Register tasks.
    grunt.registerTask('dev', ['jshint', 'clean', 'copy', 'less', 'concat']);
    grunt.registerTask('prod', ['jshint', 'clean', 'copy', 'less', 'concat', 'ngAnnotate', 'uglify'/*, 'htmlmin'*/]);
    grunt.registerTask('update', ['jshint', 'copy:app', 'less:app', 'concat:app', 'concat:app_index']);
};